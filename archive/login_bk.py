# utils/login.py

import gzip
import shutil
from pathlib import Path
import requests
import os
import json
import webbrowser
import secrets
import urllib.parse
import logging
import time
import pyotp
from urllib.parse import urlparse, parse_qs
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from dotenv import load_dotenv
from datetime import datetime, timedelta

# Load environment variables from .env file FIRST
load_dotenv()

# Contracts configuration
INSTRUMENTS_JSON = os.getenv("INSTRUMENTS_JSON")
CONTRACTS_URI = os.getenv("CONTRACTS_URI")

# Setup logging
logger = logging.getLogger(__name__)

# ANSI Color codes for console output
class Colors:
    RESET = '\033[0m'
    BOLD = '\033[1m'
    DIM = '\033[2m'
    UNDERLINE = '\033[4m'
    
    # Text Colors
    BLACK = '\033[30m'
    RED = '\033[31m'
    GREEN = '\033[32m'
    YELLOW = '\033[33m'
    BLUE = '\033[34m'
    MAGENTA = '\033[35m'
    CYAN = '\033[36m'
    WHITE = '\033[37m'
    
    # Bright Colors
    BRIGHT_BLACK = '\033[90m'
    BRIGHT_RED = '\033[91m'
    BRIGHT_GREEN = '\033[92m'
    BRIGHT_YELLOW = '\033[93m'
    BRIGHT_BLUE = '\033[94m'
    BRIGHT_MAGENTA = '\033[95m'
    BRIGHT_CYAN = '\033[96m'
    BRIGHT_WHITE = '\033[97m'
    
    # Background Colors
    BG_BLACK = '\033[40m'
    BG_RED = '\033[41m'
    BG_GREEN = '\033[42m'
    BG_YELLOW = '\033[43m'
    BG_BLUE = '\033[44m'
    BG_MAGENTA = '\033[45m'
    BG_CYAN = '\033[46m'
    BG_WHITE = '\033[47m'

# Common redirect URI (used as default for all APIs)
DEFAULT_REDIRECT_URI = os.getenv("UPSTOX_REDIRECT_URI")

# Multiple API Key Configuration
# Each API can optionally have its own redirect_uri, otherwise uses DEFAULT_REDIRECT_URI
API_CONFIGS = {
    'MARKETDATA1': {
        'api_key': os.getenv("UPSTOX_CLIENT_ID_0"),
        'api_secret': os.getenv("UPSTOX_CLIENT_SECRET_0"),
        'redirect_uri': os.getenv("UPSTOX_REDIRECT_URI_0", DEFAULT_REDIRECT_URI),
        'color': Colors.BRIGHT_BLUE,
    },
    'MARKETDATA2': {
        'api_key': os.getenv("UPSTOX_CLIENT_ID_1"),
        'api_secret': os.getenv("UPSTOX_CLIENT_SECRET_1"),
        'redirect_uri': os.getenv("UPSTOX_REDIRECT_URI_1", DEFAULT_REDIRECT_URI),
        'color': Colors.BRIGHT_MAGENTA,
    },
    'OPTIONCHAIN': {
        'api_key': os.getenv("UPSTOX_CLIENT_ID_2"),
        'api_secret': os.getenv("UPSTOX_CLIENT_SECRET_2"),
        'redirect_uri': os.getenv("UPSTOX_REDIRECT_URI_2", DEFAULT_REDIRECT_URI),
        'color': Colors.BRIGHT_CYAN,
    },
    'ORDERS': {
        'api_key': os.getenv("UPSTOX_CLIENT_ID_3"),
        'api_secret': os.getenv("UPSTOX_CLIENT_SECRET_3"),
        'redirect_uri': os.getenv("UPSTOX_REDIRECT_URI_3", DEFAULT_REDIRECT_URI),
        'color': Colors.BRIGHT_YELLOW,
    },
    'HISTORIC': {
        'api_key': os.getenv("UPSTOX_CLIENT_ID_4"),
        'api_secret': os.getenv("UPSTOX_CLIENT_SECRET_4"),
        'redirect_uri': os.getenv("UPSTOX_REDIRECT_URI_4", DEFAULT_REDIRECT_URI),
        'color': Colors.BRIGHT_RED,
    },
    'AI': {
        'api_key': os.getenv("UPSTOX_CLIENT_ID_5"),
        'api_secret': os.getenv("UPSTOX_CLIENT_SECRET_5"),
        'redirect_uri': os.getenv("UPSTOX_REDIRECT_URI_5", DEFAULT_REDIRECT_URI),
        'color': Colors.BRIGHT_RED,
    }
}

# Common configuration (kept for backward compatibility)
redirect_url_optionchain = DEFAULT_REDIRECT_URI
login_pin = os.getenv("UPSTOX_PIN")
mobile_number = os.getenv("UPSTOX_MOBILE_NUMBER")
totp = os.getenv("UPSTOX_TOTP")

REDIRECT_URI = redirect_url_optionchain
GRANT_TYPE = "authorization_code"
LOGIN_PIN = login_pin
MOBILE_NUMBER = mobile_number
TOTP = totp

# URL endpoints
AUTH_URL = "https://api.upstox.com/v2/login/authorization/dialog"
TOKEN_URL = "https://api.upstox.com/v2/login/authorization/token"
PROFILE_URL = "https://api.upstox.com/v2/user/profile"

# JSON tokens file path
TOKENS_FILE = os.getenv("TOKEN_DIR_JSON")

def calculate_token_validity(generation_time=None):
    """
    Calculate token validity based on the generation time
    Token is valid until 3:00 AM of the next day after generation
    
    Args:
        generation_time (datetime, optional): When the token was generated. 
                                            If None, uses current time.
    
    Returns:
        datetime: The validity expiration time
    """
    if generation_time is None:
        generation_time = datetime.now()
    
    # If generated before 3:00 AM, valid until 3:00 AM same day
    # If generated at or after 3:00 AM, valid until 3:00 AM next day
    three_am_today = generation_time.replace(hour=3, minute=0, second=0, microsecond=0)
    
    if generation_time < three_am_today:
        # Generated before 3:00 AM today, valid until 3:00 AM today
        validity_time = three_am_today
    else:
        # Generated at or after 3:00 AM today, valid until 3:00 AM tomorrow
        validity_time = three_am_today + timedelta(days=1)
    
    return validity_time

def is_token_valid(token_data):
    """
    Check if a token is still valid based on its validity_at timestamp
    
    Args:
        token_data (dict): Token data containing validity_at field
        
    Returns:
        bool: True if token is still valid, False otherwise
    """
    try:
        if not isinstance(token_data, dict):
            return False
        
        validity_at = token_data.get('validity_at')
        if not validity_at:
            return False
        
        validity_time = datetime.fromisoformat(validity_at)
        current_time = datetime.now()
        
        return current_time < validity_time
        
    except (ValueError, TypeError) as e:
        logger.warning(f"Error checking token validity: {e}")
        return False

def get_token_status_info(token_data):
    """
    Get detailed status information about a token
    
    Args:
        token_data (dict): Token data
        
    Returns:
        dict: Status information including validity, time remaining, etc.
    """
    try:
        if not isinstance(token_data, dict):
            return {"valid": False, "error": "Invalid token data format"}
        
        validity_at = token_data.get('validity_at')
        generated_at = token_data.get('generated_at')
        
        if not validity_at:
            return {"valid": False, "error": "No validity information"}
        
        validity_time = datetime.fromisoformat(validity_at)
        current_time = datetime.now()
        
        is_valid = current_time < validity_time
        
        if is_valid:
            time_remaining = validity_time - current_time
            hours_remaining = time_remaining.total_seconds() / 3600
            
            return {
                "valid": True,
                "validity_at": validity_at,
                "generated_at": generated_at,
                "current_time": current_time.isoformat(),
                "time_remaining_hours": round(hours_remaining, 2),
                "time_remaining_str": str(time_remaining).split('.')[0],  # Remove microseconds
                "expires_at": validity_time.strftime("%Y-%m-%d %H:%M:%S")
            }
        else:
            expired_since = current_time - validity_time
            hours_expired = expired_since.total_seconds() / 3600
            
            return {
                "valid": False,
                "validity_at": validity_at,
                "generated_at": generated_at,
                "current_time": current_time.isoformat(),
                "expired_hours_ago": round(hours_expired, 2),
                "expired_since_str": str(expired_since).split('.')[0],  # Remove microseconds
                "expired_at": validity_time.strftime("%Y-%m-%d %H:%M:%S")
            }
            
    except (ValueError, TypeError) as e:
        return {"valid": False, "error": f"Error processing token status: {e}"}

def print_colored(text, color=Colors.WHITE, bold=False, underline=False):
    """Print colored text to console"""
    style = ""
    if bold:
        style += Colors.BOLD
    if underline:
        style += Colors.UNDERLINE
    print(f"{style}{color}{text}{Colors.RESET}")

def print_profile_card(api_name, profile_data, color):
    """Print a stylized profile card"""
    email = profile_data.get('email', 'N/A')
    user_id = profile_data.get('user_id', 'N/A')
    user_name = profile_data.get('user_name', 'N/A')
    user_type = profile_data.get('user_type', 'N/A')
    is_active = profile_data.get('is_active', False)
    
    # Mask email for privacy (show first 3 chars and domain)
    if email != 'N/A' and '@' in email:
        email_parts = email.split('@')
        masked_email = f"{email_parts[0][:3]}***@{email_parts[1]}"
    else:
        masked_email = email
    
    # Status color
    status_color = Colors.BRIGHT_GREEN if is_active else Colors.BRIGHT_RED
    status_text = "ACTIVE" if is_active else "INACTIVE"
    
    # Print profile card
    print(f"\n{color}{'═' * 60}{Colors.RESET}")
    print(f"{color}{Colors.BOLD}  🔑 {api_name} API PROFILE  {Colors.RESET}")
    print(f"{color}{'─' * 60}{Colors.RESET}")
    print(f"{Colors.WHITE}  📧 Email      : {Colors.CYAN}{masked_email}{Colors.RESET}")
    print(f"{Colors.WHITE}  🆔 User ID    : {color}{Colors.BOLD}{user_id}{Colors.RESET}")
    print(f"{Colors.WHITE}  👤 Name       : {color}{Colors.BOLD}{user_name}{Colors.RESET}")
    print(f"{Colors.WHITE}  📋 Type       : {Colors.YELLOW}{user_type.upper()}{Colors.RESET}")
    print(f"{Colors.WHITE}  ✅ Status     : {status_color}{Colors.BOLD}{status_text}{Colors.RESET}")
    print(f"{color}{'═' * 60}{Colors.RESET}")

def setup_webdriver():
    """Setup Chrome WebDriver with options"""
    chrome_options = Options()
    chrome_options.add_argument("--disable-blink-features=AutomationControlled")
    chrome_options.add_experimental_option("excludeSwitches", ["enable-automation"])
    chrome_options.add_experimental_option('useAutomationExtension', False)
    
    # Add more stability options
    chrome_options.add_argument("--no-sandbox")
    chrome_options.add_argument("--disable-dev-shm-usage")
    chrome_options.add_argument("--disable-gpu")
    chrome_options.add_argument("--disable-extensions")
    chrome_options.add_argument("--disable-web-security")
    chrome_options.add_argument("--allow-running-insecure-content")
    chrome_options.add_argument("--start-maximized")
    # chrome_options.add_argument("--headless")  # Run in headless mode for servers without GUI
    
    try:
        driver = webdriver.Chrome(options=chrome_options)
        driver.execute_script("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})")
        logger.info("Chrome WebDriver initialized successfully")
        return driver
    except Exception as e:
        logger.error(f"Failed to initialize Chrome WebDriver: {e}")
        raise

def build_authorization_url(client_id, redirect_uri=None):
    """Construct the URL for user login and authorization.
    
    Args:
        client_id: The API client ID
        redirect_uri: The redirect URI for this API (defaults to REDIRECT_URI if not provided)
    
    Returns:
        tuple: (full_url, state) - The authorization URL and state parameter
    """
    # Use provided redirect_uri or fall back to default
    effective_redirect_uri = redirect_uri if redirect_uri else REDIRECT_URI
    
    state = secrets.token_urlsafe(16)  # Random string for CSRF protection
    params = {
        "client_id": client_id,
        "redirect_uri": effective_redirect_uri,
        "response_type": "code",
        "state": state
    }
    full_url = f"{AUTH_URL}?{urllib.parse.urlencode(params)}"
    logger.info(f"Authorization URL constructed successfully for client: {client_id}")
    logger.info(f"Redirect URI: {effective_redirect_uri}")
    return full_url, state

def generate_totp_code(totp_secret):
    """Generate TOTP code using the secret"""
    try:
        totp_generator = pyotp.TOTP(totp_secret)
        code = totp_generator.now()
        logger.info("TOTP code generated successfully")
        return code
    except Exception as e:
        logger.error(f"Error generating TOTP code: {e}")
        return None

def automate_login_process(auth_url, expected_state, redirect_uri=None):
    """Automate the login process using Selenium with better error handling
    
    Args:
        auth_url: The authorization URL to open
        expected_state: The expected state parameter for CSRF validation
        redirect_uri: The redirect URI for this API (defaults to REDIRECT_URI if not provided)
    """
    # Use provided redirect_uri or fall back to default
    effective_redirect_uri = redirect_uri if redirect_uri else REDIRECT_URI
    
    if not LOGIN_PIN or not MOBILE_NUMBER or not TOTP:
        logger.error("LOGIN_PIN, MOBILE_NUMBER, or TOTP not found in environment variables")
        return None
    
    driver = None
    try:
        logger.info("Setting up web driver...")
        driver = setup_webdriver()
        
        logger.info("Opening authorization URL...")
        driver.get(auth_url)
        
        # Wait for mobile number input field and enter the mobile number
        logger.info("Waiting for mobile number input field...")
        mobile_input = WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.XPATH, '//*[@id="mobileNum"]'))
        )
        
        logger.info("Entering mobile number...")
        mobile_input.clear()
        mobile_input.send_keys(MOBILE_NUMBER)
        
        # Wait for and click the Get OTP button
        logger.info("Clicking Get OTP button...")
        get_otp_button = WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="getOtp"]'))
        )
        get_otp_button.click()
        
        # Wait a bit for OTP to be sent
        logger.info("Waiting for OTP to be sent...")
        time.sleep(3)
        
        # Generate TOTP code
        logger.info("Generating TOTP code...")
        totp_code = generate_totp_code(TOTP)
        if not totp_code:
            logger.error("Failed to generate TOTP code")
            return None
        
        logger.info(f"Generated TOTP code: {totp_code}")
        
        # Wait for OTP input field and enter the TOTP code
        logger.info("Waiting for OTP input field...")
        otp_input = WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.XPATH, '//*[@id="otpNum"]'))
        )
        
        logger.info("Entering TOTP code...")
        otp_input.clear()
        otp_input.send_keys(totp_code)
        
        # Wait for and click the continue button for OTP
        logger.info("Clicking continue button for OTP...")
        continue_otp_button = WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="continueBtn"]'))
        )
        continue_otp_button.click()
        
        # Wait a bit before PIN entry
        logger.info("Waiting before PIN entry...")
        time.sleep(3)
        
        # Wait for PIN input field and enter the PIN
        logger.info("Waiting for PIN input field...")
        pin_input = WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.XPATH, '//*[@id="pinCode"]'))
        )
        
        logger.info("Entering PIN...")
        pin_input.clear()
        pin_input.send_keys(LOGIN_PIN)
        
        # Wait for and click the continue button for PIN
        logger.info("Clicking continue button for PIN...")
        continue_pin_button = WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="pinContinueBtn"]'))
        )
        continue_pin_button.click()
        
        # Wait for redirect and capture the URL
        logger.info("Waiting for redirect...")
        redirect_domain = effective_redirect_uri.split('://')[1].split('/')[0]
        logger.info(f"Waiting for redirect to domain: {redirect_domain}")
        
        def check_redirect(driver):
            current_url = driver.current_url
            logger.debug(f"Current URL: {current_url}")
            return redirect_domain in current_url
        
        WebDriverWait(driver, 60).until(check_redirect)  # Increased timeout
        
        redirected_url = driver.current_url
        logger.info(f"Captured redirect URL: {redirected_url}")
        
        # Extract the code and state from the URL
        parsed_url = urlparse(redirected_url)
        query_params = parse_qs(parsed_url.query)
        returned_code = query_params.get("code", [None])[0]
        returned_state = query_params.get("state", [None])[0]
        
        # Validate state parameter
        if returned_state != expected_state:
            logger.error("State parameter does not match! Possible CSRF attack.")
            return None
        else:
            logger.info("State parameter validated successfully")
        
        logger.info("Automated login process completed successfully")
        return returned_code
        
    except TimeoutException as e:
        logger.error(f"Timeout during login process: {e}")
        # Try to get current page source for debugging
        try:
            if driver:
                current_url = driver.current_url
                logger.debug(f"Timeout occurred at URL: {current_url}")
        except:
            pass
        return None
    except NoSuchElementException as e:
        logger.error(f"Element not found during login: {e}")
        return None
    except Exception as e:
        logger.error(f"Unexpected error during automation: {e}", exc_info=True)
        return None
    finally:
        if driver:
            try:
                driver.quit()
                logger.info("Web driver closed successfully")
            except Exception as e:
                logger.warning(f"Error closing driver: {e}")

def get_access_token(auth_code, client_id, client_secret, redirect_uri=None):
    """Exchange authorization code for access token.
    
    Args:
        auth_code: The authorization code from OAuth flow
        client_id: The API client ID  
        client_secret: The API client secret
        redirect_uri: The redirect URI for this API (defaults to REDIRECT_URI if not provided)
    """
    # Use provided redirect_uri or fall back to default
    effective_redirect_uri = redirect_uri if redirect_uri else REDIRECT_URI
    
    headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Accept': 'application/json'
    }
    
    payload = {
        'code': auth_code,
        'client_id': client_id,
        'client_secret': client_secret,
        'redirect_uri': effective_redirect_uri,
        'grant_type': GRANT_TYPE
    }

    try:
        logger.info(f"Exchanging authorization code for access token (Client: {client_id})...")
        response = requests.post(TOKEN_URL, headers=headers, data=payload, timeout=30)
        
        logger.info(f"Token exchange response - Status Code: {response.status_code}")
        logger.debug(f"Token exchange response - Text: {response.text}")
        
        response.raise_for_status()  # Raise error for non-2xx responses
        data = response.json()
        
        if "access_token" in data:
            logger.info("Access token received successfully")
            return data["access_token"], data
        else:
            logger.error(f"Access token not found in response: {data}")
            return None, data
            
    except requests.exceptions.Timeout:
        logger.error("Timeout occurred while exchanging authorization code")
        return None, {"error": "timeout"}
    except requests.exceptions.RequestException as e:
        logger.error(f"HTTP Request failed during token exchange: {e}")
        return None, {"error": str(e)}
    except Exception as e:
        logger.error(f"Unexpected error during token exchange: {e}")
        return None, {"error": str(e)}

def load_existing_tokens():
    """Load existing tokens from JSON file with new structure support"""
    try:
        if os.path.exists(TOKENS_FILE):
            with open(TOKENS_FILE, 'r', encoding='utf-8') as file:
                data = json.load(file)
                logger.info(f"Loaded existing tokens from {TOKENS_FILE}")
                return data
        else:
            logger.info(f"No existing tokens file found at {TOKENS_FILE}")
            return {"status": "success", "data": {}, "metadata": {}}
    except Exception as e:
        logger.error(f"Error loading existing tokens: {e}")
        return {"status": "success", "data": {}, "metadata": {}}

def cleanup_old_tokens():
    """Remove tokens that are expired or for APIs that no longer exist in config"""
    try:
        existing_data = load_existing_tokens()
        if not existing_data or not existing_data.get("data"):
            return
        
        current_time = datetime.now()
        cleaned_apis = []
        
        # Get list of current API names
        current_api_names = set(API_CONFIGS.keys())
        
        # Work with the data section
        token_data = existing_data["data"]
        
        for api_name in list(token_data.keys()):
            token_info = token_data[api_name]
            
            # Remove if API no longer exists in config
            if api_name not in current_api_names:
                del token_data[api_name]
                cleaned_apis.append(f"{api_name} (removed from config)")
                continue
            
            # Check if token is expired based on validity_at
            if isinstance(token_info, dict) and 'validity_at' in token_info:
                if not is_token_valid(token_info):
                    del token_data[api_name]
                    cleaned_apis.append(f"{api_name} (expired)")
                    continue
            
            # Fallback: Check if token is older than 24 hours (for tokens without validity_at)
            elif isinstance(token_info, dict) and 'generated_at' in token_info:
                try:
                    generated_time = datetime.fromisoformat(token_info['generated_at'])
                    if (current_time - generated_time) > timedelta(hours=24):
                        del token_data[api_name]
                        cleaned_apis.append(f"{api_name} (legacy expiration)")
                except (ValueError, TypeError):
                    pass  # Keep token if we can't parse the date
        
        # Save cleaned data if any changes were made
        if cleaned_apis:
            # Update metadata
            metadata = existing_data.get("metadata", {})
            metadata["last_cleanup"] = current_time.isoformat()
            existing_data["metadata"] = metadata
            
            with open(TOKENS_FILE, 'w', encoding='utf-8') as file:
                json.dump(existing_data, file, indent=2, ensure_ascii=False)
            
            logger.info(f"Cleaned up {len(cleaned_apis)} old/expired tokens")
            print_colored(f"🧹 Cleaned up expired tokens: {', '.join(cleaned_apis)}", Colors.BRIGHT_YELLOW, bold=True)
            
    except Exception as e:
        logger.error(f"Error during token cleanup: {e}")

def save_tokens_to_json(tokens_data):
    """Save all tokens to JSON file with new structure"""
    try:
        # Load existing data
        existing_data = load_existing_tokens()
        
        # Preserve existing metadata if it exists
        metadata_backup = existing_data.get("metadata", {})
        
        # Ensure we have the correct structure
        if "data" not in existing_data:
            existing_data = {"status": "success", "data": {}, "metadata": {}}
        
        # Replace/add tokens in the data section
        updated_apis = []
        for api_name, token_info in tokens_data.items():
            if api_name in existing_data["data"]:
                logger.info(f"Replacing existing token for {api_name}")
            else:
                logger.info(f"Adding new token for {api_name}")
            existing_data["data"][api_name] = token_info
            updated_apis.append(api_name)
        
        # Update metadata
        metadata = {
            "last_updated": datetime.now().isoformat(),
            "total_tokens": len(existing_data["data"]),
            "generated_by": "Multi-API Upstox Authentication Script",
            "previous_update": metadata_backup.get("last_updated", "N/A"),
            "updated_apis": updated_apis,
            "last_cleanup": metadata_backup.get("last_cleanup", datetime.now().isoformat())
        }
        existing_data["metadata"] = metadata
        
        # Ensure status is set
        existing_data["status"] = "success"
        
        # Save to JSON file
        with open(TOKENS_FILE, 'w', encoding='utf-8') as file:
            json.dump(existing_data, file, indent=2, ensure_ascii=False)
        
        logger.info(f"Tokens saved to {TOKENS_FILE}")
        print_colored(f"💾 Tokens saved to {TOKENS_FILE}", Colors.BRIGHT_GREEN, bold=True)
        print_colored(f"📝 Updated/Added tokens for: {', '.join(updated_apis)}", Colors.BRIGHT_CYAN, bold=True)
        
        return True
    except Exception as e:
        logger.error(f"Failed to save tokens to JSON file: {e}")
        print_colored(f"❌ Failed to save tokens to JSON file: {e}", Colors.BRIGHT_RED, bold=True)
        return False

def get_profile_data(access_token):
    """Get profile data from API"""
    headers = {
        'Authorization': f'Bearer {access_token}',
        'Accept': 'application/json'
    }
    
    try:
        response = requests.get(PROFILE_URL, headers=headers, timeout=30)
        
        if response.status_code == 200:
            data = response.json()
            if data.get('status') == 'success':
                return data.get('data', {})
        return None
            
    except Exception as e:
        logger.error(f"Error fetching profile data: {e}")
        return None

def manual_login_fallback(client_id, redirect_uri=None):
    """Fallback to manual login process for a specific client
    
    Args:
        client_id: The API client ID
        redirect_uri: The redirect URI for this API (defaults to REDIRECT_URI if not provided)
    """
    logger.info(f"Starting manual login fallback process for client: {client_id}")
    
    try:
        # Build authorization URL
        auth_url, expected_state = build_authorization_url(client_id, redirect_uri)
        logger.info(f"Authorization URL for manual login (Client: {client_id}):")
        print(f"\nVisit the following URL in your browser to authorize the app for {client_id}:")
        print(auth_url)
        
        # Optionally open automatically in default browser
        try:
            webbrowser.open(auth_url)
            logger.info("Opened authorization URL in default browser")
        except Exception as e:
            logger.warning(f"Failed to open browser automatically: {e}")

        # Prompt user to paste the redirect URL after login
        redirected_url = input(f"\nPaste the full URL you were redirected to after login for {client_id}: ").strip()

        if not redirected_url:
            logger.error("No redirect URL provided")
            return None

        # Extract the code and state returned from redirect
        parsed_url = urlparse(redirected_url)
        query_params = parse_qs(parsed_url.query)
        returned_code = query_params.get("code", [None])[0]
        returned_state = query_params.get("state", [None])[0]

        # Validate the state parameter to prevent CSRF attacks
        if returned_state != expected_state:
            logger.error("State parameter does not match! Possible CSRF attack.")
            return None
        else:
            logger.info("State parameter validated successfully")
        
        return returned_code
        
    except Exception as e:
        logger.error(f"Manual login fallback failed: {e}")
        return None

def validate_environment_variables():
    """Validate that all required environment variables are loaded"""
    required_common_vars = {
        'REDIRECT_URI': redirect_url_optionchain,
        'LOGIN_PIN': login_pin,
        'MOBILE_NUMBER': mobile_number,
        'TOTP': totp
    }
    
    missing_vars = []
    for var_name, var_value in required_common_vars.items():
        if not var_value:
            missing_vars.append(var_name)
    
    if missing_vars:
        logger.error("Missing required common environment variables:")
        for var in missing_vars:
            logger.error(f"  - {var}")
        return False
    
    # Check API configurations
    valid_configs = {}
    for config_name, config in API_CONFIGS.items():
        if config['api_key'] and config['api_secret']:
            valid_configs[config_name] = config
            logger.info(f"✅ {config_name} API configuration is valid")
        else:
            logger.warning(f"⚠️ {config_name} API configuration is incomplete (missing API key or secret)")
    
    if not valid_configs:
        logger.error("No valid API configurations found!")
        return False
    
    logger.info(f"Found {len(valid_configs)} valid API configurations")
    return valid_configs

def process_api_config(config_name, config, use_automation=True):
    """Process a single API configuration to get access token"""
    logger.info(f"\n{'='*50}")
    logger.info(f"Processing {config_name} API")
    logger.info(f"{'='*50}")
    print_colored(f"\n🔄 Processing {config_name} API...", config['color'], bold=True)
    
    try:
        client_id = config['api_key']
        client_secret = config['api_secret']
        redirect_uri = config.get('redirect_uri', REDIRECT_URI)  # Get per-API redirect URI
        
        logger.info(f"Using redirect URI: {redirect_uri}")
        
        # Build authorization URL with API-specific redirect URI
        auth_url, expected_state = build_authorization_url(client_id, redirect_uri)
        
        # Try automated login first if enabled
        returned_code = None
        if use_automation:
            logger.info(f"Attempting automated login for {config_name}...")
            try:
                returned_code = automate_login_process(auth_url, expected_state, redirect_uri)
            except Exception as e:
                logger.error(f"Automated login failed for {config_name}: {e}")
        
        # If automated login fails, fallback to manual process
        if not returned_code:
            logger.info(f"Automated login failed for {config_name}, falling back to manual process...")
            returned_code = manual_login_fallback(client_id, redirect_uri)
        
        # Exchange authorization code for access token with API-specific redirect URI
        if returned_code:
            logger.info(f"Exchanging authorization code for access token ({config_name})...")
            token, response_data = get_access_token(returned_code, client_id, client_secret, redirect_uri)
            
            if token:
                logger.info(f"✅ {config_name} - Access token generated successfully!")
                print_colored(f"✅ {config_name} - Access token generated successfully!", Colors.BRIGHT_GREEN, bold=True)
                
                return True, token
            else:
                logger.error(f"❌ {config_name} - Failed to get access token: {response_data}")
                print_colored(f"❌ {config_name} - Failed to get access token", Colors.BRIGHT_RED, bold=True)
                return False, None
        else:
            logger.error(f"❌ {config_name} - Authorization code not found")
            print_colored(f"❌ {config_name} - Authorization code not found", Colors.BRIGHT_RED, bold=True)
            return False, None
            
    except Exception as e:
        logger.error(f"❌ {config_name} - Unexpected error: {e}", exc_info=True)
        print_colored(f"❌ {config_name} - Unexpected error: {e}", Colors.BRIGHT_RED, bold=True)
        return False, None

def display_all_profiles(successful_tokens):
    """Display all user profiles in a stylized format"""
    if not successful_tokens:
        return
    
    print_colored(f"\n{'═' * 80}", Colors.BRIGHT_WHITE, bold=True)
    print_colored("🎯 USER PROFILE VERIFICATION", Colors.BRIGHT_WHITE, bold=True, underline=True)
    print_colored(f"{'═' * 80}", Colors.BRIGHT_WHITE, bold=True)
    
    for api_name, (token, color) in successful_tokens.items():
        profile_data = get_profile_data(token)
        if profile_data:
            print_profile_card(api_name, profile_data, color)
        else:
            print_colored(f"\n❌ Failed to fetch profile for {api_name}", Colors.BRIGHT_RED, bold=True)
    
    print_colored(f"\n{'═' * 80}", Colors.BRIGHT_WHITE, bold=True)

def display_token_file_location():
    """Display information about the tokens file location"""
    abs_path = os.path.abspath(TOKENS_FILE)
    print_colored(f"\n📁 TOKEN FILE INFORMATION", Colors.BRIGHT_CYAN, bold=True, underline=True)
    print_colored(f"{'─' * 60}", Colors.BRIGHT_CYAN)
    print_colored(f"📄 File Name: {TOKENS_FILE}", Colors.BRIGHT_WHITE, bold=True)
    print_colored(f"📂 Full Path: {abs_path}", Colors.BRIGHT_WHITE)
    print_colored(f"📝 Format: JSON", Colors.BRIGHT_WHITE)
    print_colored(f"💡 Usage: Import this file in your project to access tokens", Colors.BRIGHT_YELLOW)
    print_colored(f"{'─' * 60}", Colors.BRIGHT_CYAN)

def display_token_validity_info(tokens_data):
    """Display token validity information for all tokens"""
    if not tokens_data:
        return
    
    print_colored(f"\n⏰ TOKEN VALIDITY INFORMATION", Colors.BRIGHT_CYAN, bold=True, underline=True)
    print_colored(f"{'─' * 80}", Colors.BRIGHT_CYAN)
    
    for api_name, token_data in tokens_data.items():
        status_info = get_token_status_info(token_data)
        color = API_CONFIGS.get(api_name, {}).get('color', Colors.BRIGHT_WHITE)
        
        print(f"\n{color}{'─' * 50}{Colors.RESET}")
        print(f"{color}{Colors.BOLD}  🔑 {api_name} TOKEN STATUS  {Colors.RESET}")
        print(f"{color}{'─' * 50}{Colors.RESET}")
        
        if status_info.get('valid'):
            print(f"{Colors.WHITE}  ✅ Status      : {Colors.BRIGHT_GREEN}{Colors.BOLD}VALID{Colors.RESET}")
            print(f"{Colors.WHITE}  ⏰ Expires at  : {Colors.BRIGHT_CYAN}{status_info['expires_at']}{Colors.RESET}")
            print(f"{Colors.WHITE}  ⏳ Time left   : {Colors.BRIGHT_YELLOW}{status_info['time_remaining_str']}{Colors.RESET}")
            print(f"{Colors.WHITE}  📊 Hours left  : {Colors.BRIGHT_YELLOW}{status_info['time_remaining_hours']}{Colors.RESET}")
        else:
            print(f"{Colors.WHITE}  ❌ Status      : {Colors.BRIGHT_RED}{Colors.BOLD}EXPIRED{Colors.RESET}")
            if 'expired_at' in status_info:
                print(f"{Colors.WHITE}  ⏰ Expired at  : {Colors.BRIGHT_RED}{status_info['expired_at']}{Colors.RESET}")
                print(f"{Colors.WHITE}  ⏳ Expired ago : {Colors.BRIGHT_RED}{status_info['expired_since_str']}{Colors.RESET}")
                print(f"{Colors.WHITE}  📊 Hours ago   : {Colors.BRIGHT_RED}{status_info['expired_hours_ago']}{Colors.RESET}")
            if 'error' in status_info:
                print(f"{Colors.WHITE}  ⚠️  Error      : {Colors.BRIGHT_YELLOW}{status_info['error']}{Colors.RESET}")
        
        if 'generated_at' in token_data:
            generated_at = datetime.fromisoformat(token_data['generated_at'])
            print(f"{Colors.WHITE}  📅 Generated   : {Colors.BRIGHT_WHITE}{generated_at.strftime('%Y-%m-%d %H:%M:%S')}{Colors.RESET}")
    
    print_colored(f"\n{'─' * 80}", Colors.BRIGHT_CYAN)

def download_contracts(save_path=None):
    """
    Download and extract contracts from .gz format
    
    Args:
        save_path (str, optional): Custom path to save the contracts. 
                                 If None, uses INSTRUMENTS_JSON from .env
    
    Returns:
        tuple: (success: bool, file_path: str, message: str)
    """
    if not CONTRACTS_URI:
        error_msg = "CONTRACTS_URI not found in environment variables"
        logger.error(error_msg)
        print_colored(f"❌ {error_msg}", Colors.BRIGHT_RED, bold=True)
        return False, None, error_msg
    
    # Determine save path
    final_save_path = save_path or INSTRUMENTS_JSON
    if not final_save_path:
        error_msg = "No save path specified and INSTRUMENTS_JSON not found in environment variables"
        logger.error(error_msg)
        print_colored(f"❌ {error_msg}", Colors.BRIGHT_RED, bold=True)
        return False, None, error_msg
    
    try:
        logger.info("Starting contracts download process...")
        print_colored("📥 Starting contracts download...", Colors.BRIGHT_CYAN, bold=True)
        
        # Create directory if it doesn't exist
        save_dir = Path(final_save_path).parent
        save_dir.mkdir(parents=True, exist_ok=True)
        
        # Download the .gz file
        logger.info(f"Downloading contracts from: {CONTRACTS_URI}")
        print_colored(f"🌐 Downloading from: {CONTRACTS_URI}", Colors.BRIGHT_BLUE)
        
        response = requests.get(CONTRACTS_URI, stream=True, timeout=120)
        response.raise_for_status()
        
        # Get file size for progress tracking
        total_size = int(response.headers.get('content-length', 0))
        
        # Temporary file for .gz download
        temp_gz_path = f"{final_save_path}.gz"
        
        # Download with progress indication
        downloaded_size = 0
        with open(temp_gz_path, 'wb') as temp_file:
            for chunk in response.iter_content(chunk_size=8192):
                if chunk:
                    temp_file.write(chunk)
                    downloaded_size += len(chunk)
                    
                    if total_size > 0:
                        progress = (downloaded_size / total_size) * 100
                        print(f"\r📊 Download progress: {progress:.1f}% ({downloaded_size:,} / {total_size:,} bytes)", end='', flush=True)
        
        print()  # New line after progress
        logger.info(f"Downloaded {downloaded_size:,} bytes")
        print_colored(f"✅ Downloaded {downloaded_size:,} bytes", Colors.BRIGHT_GREEN)
        
        # Extract the .gz file
        logger.info("Extracting contracts from .gz file...")
        print_colored("📦 Extracting contracts...", Colors.BRIGHT_YELLOW)
        
        with gzip.open(temp_gz_path, 'rb') as gz_file:
            with open(final_save_path, 'wb') as json_file:
                shutil.copyfileobj(gz_file, json_file)
        
        # Clean up temporary .gz file
        os.remove(temp_gz_path)
        logger.info("Temporary .gz file cleaned up")
        
        # Verify the extracted file
        if os.path.exists(final_save_path):
            file_size = os.path.getsize(final_save_path)
            logger.info(f"Contracts successfully extracted to: {final_save_path}")
            logger.info(f"Extracted file size: {file_size:,} bytes")
            
            print_colored("🎉 Contracts download and extraction completed!", Colors.BRIGHT_GREEN, bold=True)
            print_colored(f"📄 File saved to: {final_save_path}", Colors.BRIGHT_CYAN)
            print_colored(f"📏 File size: {file_size:,} bytes", Colors.BRIGHT_CYAN)
            
            # Validate JSON structure
            try:
                with open(final_save_path, 'r', encoding='utf-8') as f:
                    json.load(f)
                print_colored("✅ JSON structure validated successfully", Colors.BRIGHT_GREEN)
                logger.info("JSON structure validation passed")
            except json.JSONDecodeError as e:
                logger.warning(f"JSON validation warning: {e}")
                print_colored(f"⚠️ JSON validation warning: {e}", Colors.BRIGHT_YELLOW)
            
            return True, final_save_path, "Contracts downloaded and extracted successfully"
        else:
            error_msg = "Extraction failed - file not found after extraction"
            logger.error(error_msg)
            print_colored(f"❌ {error_msg}", Colors.BRIGHT_RED, bold=True)
            return False, None, error_msg
            
    except requests.exceptions.Timeout:
        error_msg = "Timeout occurred while downloading contracts"
        logger.error(error_msg)
        print_colored(f"⏰ {error_msg}", Colors.BRIGHT_RED, bold=True)
        return False, None, error_msg
        
    except requests.exceptions.RequestException as e:
        error_msg = f"HTTP Request failed during contracts download: {e}"
        logger.error(error_msg)
        print_colored(f"🌐 {error_msg}", Colors.BRIGHT_RED, bold=True)
        return False, None, error_msg
        
    except gzip.BadGzipFile:
        error_msg = "Invalid .gz file format"
        logger.error(error_msg)
        print_colored(f"📦 {error_msg}", Colors.BRIGHT_RED, bold=True)
        # Clean up temp file if it exists
        temp_gz_path = f"{final_save_path}.gz"
        if os.path.exists(temp_gz_path):
            os.remove(temp_gz_path)
        return False, None, error_msg
        
    except PermissionError as e:
        error_msg = f"Permission denied accessing file: {e}"
        logger.error(error_msg)
        print_colored(f"🔒 {error_msg}", Colors.BRIGHT_RED, bold=True)
        return False, None, error_msg
        
    except Exception as e:
        error_msg = f"Unexpected error during contracts download: {e}"
        logger.error(error_msg, exc_info=True)
        print_colored(f"💥 {error_msg}", Colors.BRIGHT_RED, bold=True)
        
        # Clean up temp file if it exists
        temp_gz_path = f"{final_save_path}.gz"
        if os.path.exists(temp_gz_path):
            try:
                os.remove(temp_gz_path)
                logger.info("Cleaned up temporary .gz file after error")
            except:
                pass
        
        return False, None, error_msg

def get_contracts_info():
    """
    Get information about the contracts file
    
    Returns:
        dict: Information about contracts file including size, modification time, etc.
    """
    if not INSTRUMENTS_JSON or not os.path.exists(INSTRUMENTS_JSON):
        return {
            "exists": False,
            "file_path": INSTRUMENTS_JSON or "Not configured",
            "message": "Contracts file not found"
        }
    
    try:
        stat = os.stat(INSTRUMENTS_JSON)
        return {
            "exists": True,
            "file_path": INSTRUMENTS_JSON,
            "size_bytes": stat.st_size,
            "size_mb": round(stat.st_size / (1024 * 1024), 2),
            "modified_time": datetime.fromtimestamp(stat.st_mtime).isoformat(),
            "is_readable": os.access(INSTRUMENTS_JSON, os.R_OK),
            "absolute_path": os.path.abspath(INSTRUMENTS_JSON)
        }
    except Exception as e:
        return {
            "exists": True,
            "file_path": INSTRUMENTS_JSON,
            "error": str(e),
            "message": f"Error getting file info: {e}"
        }

def load_contracts():
    """
    Load contracts from the JSON file
    
    Returns:
        tuple: (success: bool, data: dict/list, message: str)
    """
    if not INSTRUMENTS_JSON or not os.path.exists(INSTRUMENTS_JSON):
        return False, None, "Contracts file not found"
    
    try:
        with open(INSTRUMENTS_JSON, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        logger.info("Contracts loaded successfully")
        return True, data, "Contracts loaded successfully"
        
    except json.JSONDecodeError as e:
        error_msg = f"Invalid JSON format: {e}"
        logger.error(error_msg)
        return False, None, error_msg
        
    except Exception as e:
        error_msg = f"Error loading contracts: {e}"
        logger.error(error_msg)
        return False, None, error_msg

def validate_contracts_environment():
    """
    Validate contracts-related environment variables
    
    Returns:
        tuple: (valid: bool, message: str)
    """
    if not CONTRACTS_URI:
        return False, "CONTRACTS_URI not found in environment variables"
    
    if not INSTRUMENTS_JSON:
        return False, "INSTRUMENTS_JSON not found in environment variables"
    
    # Validate URI format
    if not CONTRACTS_URI.startswith(('http://', 'https://')):
        return False, "CONTRACTS_URI must be a valid HTTP/HTTPS URL"
    
    # Check if save directory is writable
    try:
        save_dir = Path(INSTRUMENTS_JSON).parent
        if not save_dir.exists():
            save_dir.mkdir(parents=True, exist_ok=True)
        
        # Test write permissions
        test_file = save_dir / "test_write_permission.tmp"
        test_file.touch()
        test_file.unlink()
        
    except Exception as e:
        return False, f"Cannot write to contracts directory: {e}"
    
    return True, "Contracts environment validation passed"

def display_contracts_info():
    """Display contracts file information in a stylized format"""
    info = get_contracts_info()
    
    print_colored(f"\n📋 CONTRACTS FILE INFORMATION", Colors.BRIGHT_CYAN, bold=True, underline=True)
    print_colored(f"{'─' * 60}", Colors.BRIGHT_CYAN)
    
    if info["exists"]:
        print_colored(f"📄 File Path: {info['file_path']}", Colors.BRIGHT_WHITE)
        print_colored(f"📂 Full Path: {info['absolute_path']}", Colors.BRIGHT_WHITE)
        print_colored(f"📏 Size: {info['size_mb']} MB ({info['size_bytes']:,} bytes)", Colors.BRIGHT_WHITE)
        print_colored(f"🕒 Modified: {info['modified_time']}", Colors.BRIGHT_WHITE)
        
        readable_status = "✅ Yes" if info['is_readable'] else "❌ No"
        print_colored(f"👁️ Readable: {readable_status}", Colors.BRIGHT_WHITE)
        
        if 'error' in info:
            print_colored(f"⚠️ Warning: {info['error']}", Colors.BRIGHT_YELLOW)
    else:
        print_colored(f"❌ File not found: {info['file_path']}", Colors.BRIGHT_RED)
        print_colored(f"💡 Run download_contracts() to fetch the latest data", Colors.BRIGHT_YELLOW)
    
    print_colored(f"{'─' * 60}", Colors.BRIGHT_CYAN)

def save_token(access_token: str, api_name: str = "MAIN"):
    """
    Compatibility function to save a single token
    """
    from utils.token_helpers import save_token as helper_save_token
    return helper_save_token(access_token, api_name)

# Modified main function to include contracts option
def main():
    """Main function for standalone execution with contracts option"""
    # Setup logging for standalone execution with UTF-8 encoding to handle Unicode characters
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.FileHandler('multi_login.log', encoding='utf-8'),
            logging.StreamHandler()
        ]
    )
    
    logger.info("Starting Multi-API Upstox authentication process...")
    print_colored("🚀 Starting Multi-API Upstox Authentication Process", Colors.BRIGHT_CYAN, bold=True, underline=True)
    
    try:
        # Clean up old tokens first
        cleanup_old_tokens()
        
        # Validate environment variables
        valid_configs = validate_environment_variables()
        if not valid_configs:
            logger.error("Environment validation failed")
            print_colored("❌ ERROR: Environment validation failed", Colors.BRIGHT_RED, bold=True)
            return 1
        
        # Check contracts environment
        contracts_valid, contracts_msg = validate_contracts_environment()
        if contracts_valid:
            print_colored(f"✅ Contracts environment: {contracts_msg}", Colors.BRIGHT_GREEN)
        else:
            print_colored(f"⚠️ Contracts environment: {contracts_msg}", Colors.BRIGHT_YELLOW)
        
        # Ask user what they want to do
        print_colored(f"\n🔧 Available Operations:", Colors.BRIGHT_WHITE, bold=True)
        print_colored("1. Generate API tokens only", Colors.BRIGHT_CYAN)
        print_colored("2. Download contracts only", Colors.BRIGHT_MAGENTA)
        print_colored("3. Generate tokens AND download contracts", Colors.BRIGHT_GREEN)
        print_colored("4. Show contracts info", Colors.BRIGHT_YELLOW)
        print_colored("5. Show token validity info", Colors.BRIGHT_BLUE)
        
        try:
            choice = input(f"\n{Colors.BRIGHT_WHITE}Enter your choice (1-5) [default: 3]: {Colors.RESET}").strip()
            if not choice:
                choice = "3"
        except KeyboardInterrupt:
            print_colored("\n\n⚠️ Process interrupted by user", Colors.BRIGHT_YELLOW, bold=True)
            return 130
        
        if choice == "4":
            display_contracts_info()
            return 0
        elif choice == "5":
            # Show token validity info
            existing_data = load_existing_tokens()
            tokens_data = existing_data.get("data", {})
            if tokens_data:
                display_token_validity_info(tokens_data)
            else:
                print_colored("❌ No tokens found to display validity information", Colors.BRIGHT_RED, bold=True)
                print_colored("💡 Generate tokens first using option 1 or 3", Colors.BRIGHT_YELLOW)
            return 0
        elif choice == "2":
            # Download contracts only
            if contracts_valid:
                success, file_path, message = download_contracts()
                if success:
                    display_contracts_info()
                    return 0
                else:
                    print_colored(f"❌ Contracts download failed: {message}", Colors.BRIGHT_RED, bold=True)
                    return 1
            else:
                print_colored(f"❌ Cannot download contracts: {contracts_msg}", Colors.BRIGHT_RED, bold=True)
                return 1
        elif choice not in ["1", "3"]:
            print_colored("❌ Invalid choice. Please select 1, 2, 3, 4, or 5.", Colors.BRIGHT_RED, bold=True)
            return 1
        
        # Continue with token generation (choice 1 or 3)
        print_colored(f"\n✅ Found {len(valid_configs)} valid API configurations:", Colors.BRIGHT_GREEN, bold=True)
        for i, (config_name, config) in enumerate(valid_configs.items(), 1):
            print_colored(f"  {i}. {config_name}", config['color'], bold=True)
        
        # Automatically use automated login (no user prompt)
        use_automation = True
        print_colored(f"\n🔧 Starting automated login process for all {len(valid_configs)} API configurations...", Colors.BRIGHT_YELLOW, bold=True)
        
        # Process each API configuration
        successful_configs = []
        failed_configs = []
        successful_tokens = {}  # Store tokens for profile verification
        tokens_to_save = {}  # Store tokens for JSON file
        
        for i, (config_name, config) in enumerate(valid_configs.items(), 1):
            print_colored(f"\n[{i}/{len(valid_configs)}] Processing {config_name}...", Colors.BRIGHT_WHITE, bold=True)
            success, token = process_api_config(config_name, config, use_automation)
            if success and token:
                successful_configs.append(config_name)
                successful_tokens[config_name] = (token, config['color'])
                
                # Calculate token validity
                generation_time = datetime.now()
                validity_time = calculate_token_validity(generation_time)
                
                # Prepare token data for JSON file
                tokens_to_save[config_name] = {
                    "access_token": token,
                    "api_key": config['api_key'],
                    "generated_at": generation_time.isoformat(),
                    "validity_at": validity_time.isoformat(),
                    "status": "active"
                }
                
                # Display validity information
                print_colored(f"⏰ Token valid until: {validity_time.strftime('%Y-%m-%d %H:%M:%S')}", Colors.BRIGHT_CYAN, bold=True)
                
            else:
                failed_configs.append(config_name)
            
            # Add a small delay between API calls to avoid rate limiting
            if i < len(valid_configs):
                print_colored("⏳ Waiting 3 seconds before next API...", Colors.BRIGHT_BLACK)
                time.sleep(3)
        
        # Save all tokens to JSON file
        if tokens_to_save:
            save_tokens_to_json(tokens_to_save)
            display_token_file_location()
            # Display token validity information
            display_token_validity_info(tokens_to_save)
        
        # Display all profiles after token generation
        if successful_tokens:
            display_all_profiles(successful_tokens)
        
        # Download contracts if choice was 3 and contracts environment is valid
        if choice == "3" and contracts_valid:
            print_colored(f"\n{'═' * 60}", Colors.BRIGHT_CYAN, bold=True)
            print_colored("📥 DOWNLOADING CONTRACTS", Colors.BRIGHT_CYAN, bold=True, underline=True)
            print_colored(f"{'═' * 60}", Colors.BRIGHT_CYAN, bold=True)
            
            success, file_path, message = download_contracts()
            if success:
                display_contracts_info()
            else:
                print_colored(f"❌ Contracts download failed: {message}", Colors.BRIGHT_RED, bold=True)
        
        # Summary
        print_colored(f"\n{'═' * 80}", Colors.BRIGHT_WHITE, bold=True)
        print_colored("📊 FINAL SUMMARY", Colors.BRIGHT_WHITE, bold=True, underline=True)
        print_colored(f"{'═' * 80}", Colors.BRIGHT_WHITE, bold=True)
        
        if successful_configs:
            print_colored(f"✅ Successfully processed ({len(successful_configs)}):", Colors.BRIGHT_GREEN, bold=True)
            for config_name in successful_configs:
                color = valid_configs[config_name]['color']
                print_colored(f"   • {config_name}", color, bold=True)
        
        if failed_configs:
            print_colored(f"❌ Failed to process ({len(failed_configs)}):", Colors.BRIGHT_RED, bold=True)
            for config_name in failed_configs:
                print_colored(f"   • {config_name}", Colors.BRIGHT_RED)
        
        if successful_configs and not failed_configs:
            logger.info("All API configurations processed successfully!")
            print_colored(f"\n🎉 All {len(successful_configs)} API configurations processed successfully!", Colors.BRIGHT_GREEN, bold=True)
            return 0
        elif successful_configs:
            logger.warning("Some API configurations failed to process")
            print_colored(f"\n⚠️  {len(successful_configs)}/{len(valid_configs)} configurations processed successfully", Colors.BRIGHT_YELLOW, bold=True)
            return 2
        else:
            logger.error("All API configurations failed to process")
            print_colored(f"\n💥 All API configurations failed to process", Colors.BRIGHT_RED, bold=True)
            return 1
            
    except KeyboardInterrupt:
        logger.info("Process interrupted by user")
        print_colored("\n\n⚠️ Process interrupted by user", Colors.BRIGHT_YELLOW, bold=True)
        return 130
    except Exception as e:
        logger.error(f"Unexpected error in main process: {e}", exc_info=True)
        print_colored(f"\n💥 Unexpected error: {e}", Colors.BRIGHT_RED, bold=True)
        return 1

# Updated utility functions for external usage with new JSON structure and validity checking
def load_tokens():
    """Load tokens from JSON file for external usage"""
    try:
        with open(TOKENS_FILE, 'r', encoding='utf-8') as file:
            data = json.load(file)
            # Return tokens from the 'data' field in new format
            if "status" in data and "data" in data:
                return data["data"]
            else:
                # Handle old format fallback (remove metadata for cleaner access)
                tokens = {k: v for k, v in data.items() if k != "metadata"}
                return tokens
    except FileNotFoundError:
        logger.warning(f"Tokens file {TOKENS_FILE} not found")
        return {}
    except Exception as e:
        logger.error(f"Error loading tokens: {e}")
        return {}

def get_token(api_name, check_validity=True):
    """
    Get a specific token by API name
    
    Args:
        api_name (str): Name of the API configuration
        check_validity (bool): Whether to check if token is still valid
        
    Returns:
        str or None: Access token if found and valid, None otherwise
    """
    tokens = load_tokens()
    if api_name in tokens:
        token_data = tokens[api_name]
        
        if check_validity and not is_token_valid(token_data):
            logger.warning(f"Token for {api_name} has expired")
            return None
            
        return token_data.get('access_token')
    return None

def get_all_active_tokens(check_validity=True):
    """
    Get all active tokens as a dictionary
    
    Args:
        check_validity (bool): Whether to check if tokens are still valid
        
    Returns:
        dict: Dictionary of API names and their access tokens
    """
    tokens = load_tokens()
    active_tokens = {}
    
    for api_name, token_data in tokens.items():
        if token_data.get('status') == 'active':
            if check_validity and not is_token_valid(token_data):
                logger.warning(f"Token for {api_name} has expired")
                continue
            active_tokens[api_name] = token_data.get('access_token')
    
    return active_tokens

def get_valid_tokens():
    """Get only valid (non-expired) tokens"""
    return get_all_active_tokens(check_validity=True)

def get_expired_tokens():
    """Get only expired tokens"""
    tokens = load_tokens()
    expired_tokens = {}
    
    for api_name, token_data in tokens.items():
        if not is_token_valid(token_data):
            expired_tokens[api_name] = token_data
    
    return expired_tokens

def is_token_file_exists():
    """Check if tokens file exists"""
    return os.path.exists(TOKENS_FILE)

def get_token_file_info():
    """Get information about the tokens file"""
    if not os.path.exists(TOKENS_FILE):
        return None
    
    try:
        with open(TOKENS_FILE, 'r', encoding='utf-8') as file:
            data = json.load(file)
            return data.get('metadata', {})
    except Exception as e:
        logger.error(f"Error reading token file info: {e}")
        return None
    
def get_complete_response():
    """Get the complete JSON response including status, data, and metadata"""
    try:
        with open(TOKENS_FILE, 'r', encoding='utf-8') as file:
            data = json.load(file)
            # Ensure we have the correct structure
            if "status" not in data:
                # Convert old format to new format
                if "metadata" in data:
                    metadata = data.pop("metadata")
                    tokens_data = data
                else:
                    metadata = {}
                    tokens_data = {k: v for k, v in data.items() if k != "metadata"}
                
                new_structure = {
                    "status": "success",
                    "data": tokens_data,
                    "metadata": metadata
                }
                return new_structure
            else:
                return data
    except FileNotFoundError:
        logger.warning(f"Tokens file {TOKENS_FILE} not found")
        return {"status": "error", "message": "Tokens file not found", "data": {}, "metadata": {}}
    except Exception as e:
        logger.error(f"Error loading complete response: {e}")
        return {"status": "error", "message": str(e), "data": {}, "metadata": {}}

def get_response_status():
    """Get the status from the JSON response"""
    try:
        response = get_complete_response()
        return response.get("status", "unknown")
    except Exception as e:
        logger.error(f"Error getting response status: {e}")
        return "error"

def get_tokens_data():
    """Get only the data section from JSON response"""
    try:
        response = get_complete_response()
        return response.get("data", {})
    except Exception as e:
        logger.error(f"Error getting tokens data: {e}")
        return {}

def get_metadata():
    """Get only the metadata section from JSON response"""
    try:
        response = get_complete_response()
        return response.get("metadata", {})
    except Exception as e:
        logger.error(f"Error getting metadata: {e}")
        return {}

def save_error_response(error_message):
    """Save error response to JSON file"""
    try:
        error_response = {
            "status": "error",
            "message": error_message,
            "data": {},
            "metadata": {
                "last_updated": datetime.now().isoformat(),
                "total_tokens": 0,
                "generated_by": "Multi-API Upstox Authentication Script",
                "error_occurred_at": datetime.now().isoformat()
            }
        }
        
        with open(TOKENS_FILE, 'w', encoding='utf-8') as file:
            json.dump(error_response, file, indent=2, ensure_ascii=False)
        
        logger.info(f"Error response saved to {TOKENS_FILE}")
        return True
    except Exception as e:
        logger.error(f"Failed to save error response: {e}")
        return False

def validate_json_structure():
    """Validate that the JSON file has the correct structure"""
    try:
        response = get_complete_response()
        
        # Check required fields
        required_fields = ["status", "data", "metadata"]
        for field in required_fields:
            if field not in response:
                return False, f"Missing required field: {field}"
        
        # Check data types
        if not isinstance(response["data"], dict):
            return False, "Data field must be a dictionary"
        
        if not isinstance(response["metadata"], dict):
            return False, "Metadata field must be a dictionary"
        
        # Check status values
        valid_statuses = ["success", "error", "partial"]
        if response["status"] not in valid_statuses:
            return False, f"Invalid status. Must be one of: {valid_statuses}"
        
        return True, "JSON structure is valid"
        
    except Exception as e:
        return False, f"Error validating JSON structure: {e}"

def check_all_tokens_validity():
    """
    Check validity of all tokens and return a summary
    
    Returns:
        dict: Summary of token validity status
    """
    tokens = load_tokens()
    
    if not tokens:
        return {
            "total_tokens": 0,
            "valid_tokens": 0,
            "expired_tokens": 0,
            "tokens_without_validity": 0,
            "summary": "No tokens found"
        }
    
    valid_count = 0
    expired_count = 0
    no_validity_count = 0
    
    token_details = {}
    
    for api_name, token_data in tokens.items():
        if 'validity_at' not in token_data:
            no_validity_count += 1
            token_details[api_name] = {"status": "no_validity_info", "valid": False}
        elif is_token_valid(token_data):
            valid_count += 1
            token_details[api_name] = {"status": "valid", "valid": True}
        else:
            expired_count += 1
            token_details[api_name] = {"status": "expired", "valid": False}
    
    return {
        "total_tokens": len(tokens),
        "valid_tokens": valid_count,
        "expired_tokens": expired_count,
        "tokens_without_validity": no_validity_count,
        "token_details": token_details,
        "summary": f"{valid_count} valid, {expired_count} expired, {no_validity_count} without validity info"
    }

def refresh_expired_tokens():
    """
    Check for expired tokens and prompt user to refresh them
    
    Returns:
        dict: Information about expired tokens that need refresh
    """
    expired_tokens = get_expired_tokens()
    
    if not expired_tokens:
        return {"expired_count": 0, "message": "No expired tokens found"}
    
    expired_info = {}
    for api_name, token_data in expired_tokens.items():
        status_info = get_token_status_info(token_data)
        expired_info[api_name] = {
            "expired_at": status_info.get("expired_at", "Unknown"),
            "expired_hours_ago": status_info.get("expired_hours_ago", 0),
            "needs_refresh": True
        }
    
    return {
        "expired_count": len(expired_tokens),
        "expired_apis": list(expired_tokens.keys()),
        "expired_info": expired_info,
        "message": f"Found {len(expired_tokens)} expired tokens that need refresh"
    }

def get_token_with_auto_check(api_name):
    """
    Get token with automatic validity check and user notification
    
    Args:
        api_name (str): Name of the API configuration
        
    Returns:
        tuple: (token_or_none, status_message)
    """
    try:
        tokens = load_tokens()
        
        if api_name not in tokens:
            return None, f"Token for {api_name} not found"
        
        token_data = tokens[api_name]
        
        # Check if token has validity information
        if 'validity_at' not in token_data:
            logger.warning(f"Token for {api_name} does not have validity information")
            return token_data.get('access_token'), f"Token found but validity unknown - consider regenerating"
        
        # Check if token is still valid
        if is_token_valid(token_data):
            status_info = get_token_status_info(token_data)
            hours_left = status_info.get('time_remaining_hours', 0)
            
            if hours_left < 1:  # Less than 1 hour left
                return token_data.get('access_token'), f"Token expires soon ({hours_left:.1f} hours left)"
            else:
                return token_data.get('access_token'), f"Token valid ({hours_left:.1f} hours left)"
        else:
            status_info = get_token_status_info(token_data)
            expired_hours = status_info.get('expired_hours_ago', 0)
            return None, f"Token expired {expired_hours:.1f} hours ago - regeneration required"
            
    except Exception as e:
        logger.error(f"Error checking token for {api_name}: {e}")
        return None, f"Error accessing token: {e}"

def display_token_expiry_warning():
    """Display warning for tokens that are about to expire"""
    tokens = load_tokens()
    warning_tokens = []
    
    for api_name, token_data in tokens.items():
        if is_token_valid(token_data):
            status_info = get_token_status_info(token_data)
            hours_left = status_info.get('time_remaining_hours', float('inf'))
            
            if hours_left <= 2:  # Warning if less than 2 hours left
                warning_tokens.append({
                    'api_name': api_name,
                    'hours_left': hours_left,
                    'expires_at': status_info.get('expires_at', 'Unknown')
                })
    
    if warning_tokens:
        print_colored(f"\n⚠️  TOKEN EXPIRY WARNING", Colors.BRIGHT_YELLOW, bold=True, underline=True)
        print_colored(f"{'─' * 60}", Colors.BRIGHT_YELLOW)
        
        for token_info in warning_tokens:
            api_name = token_info['api_name']
            hours_left = token_info['hours_left']
            expires_at = token_info['expires_at']
            
            if hours_left < 1:
                urgency_color = Colors.BRIGHT_RED
                urgency_text = "URGENT"
            else:
                urgency_color = Colors.BRIGHT_YELLOW
                urgency_text = "WARNING"
            
            print_colored(f"🔑 {api_name}: {urgency_text} - Expires at {expires_at} ({hours_left:.1f}h left)", urgency_color, bold=True)
        
        print_colored(f"{'─' * 60}", Colors.BRIGHT_YELLOW)
        print_colored("💡 Run the script to regenerate expired tokens", Colors.BRIGHT_CYAN)

# Example usage functions for external scripts
def example_usage():
    """Example usage of the token functions"""
    print("=== Token Management Examples ===")
    
    # Check if tokens file exists
    if is_token_file_exists():
        print("✅ Tokens file exists")
        
        # Get all valid tokens
        valid_tokens = get_valid_tokens()
        print(f"📊 Found {len(valid_tokens)} valid tokens")
        
        # Check specific token
        for api_name in ['MARKETDATA1', 'OPTIONCHAIN']:
            token, message = get_token_with_auto_check(api_name)
            if token:
                print(f"✅ {api_name}: {message}")
            else:
                print(f"❌ {api_name}: {message}")
        
        # Display expiry warnings
        display_token_expiry_warning()
        
        # Get validity summary
        validity_summary = check_all_tokens_validity()
        print(f"📋 Token Summary: {validity_summary['summary']}")
        
    else:
        print("❌ No tokens file found - run authentication first")

if __name__ == "__main__":
    exit_code = main()
    exit(exit_code)