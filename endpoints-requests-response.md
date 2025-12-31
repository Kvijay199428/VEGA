Production Base Url's: "V2"={https://api.upstox.com/v2}, "V3"={https://api.upstox.com/v3}, "HFTV3"={https://api-hft.upstox.com/v3}
Sandbox Base Url: "SANDBOX"={https://api-sandbox.upstox.com/v3}

Frontend URL endpoint construction = http://localhost:{port}/user/

GET /user/profile: V2 ---> Get Profile
	Request:
		curl --location 'https://api.upstox.com/v2/user/profile' \
		--header 'Content-Type: application/json' \
		--header 'Accept: application/json' \
		--header 'Authorization: Bearer {your_access_token}'
	Response:
		{
            "status": "success",
            "data": {
                "email": "******",
                "exchanges": ["NSE", "NFO", "BSE", "CDS", "BFO", "BCD"],
                "products": ["D", "CO", "I"],
                "broker": "UPSTOX",
                "user_id": "******",
                "user_name": "******",
                "order_types": ["MARKET", "LIMIT", "SL", "SL-M"],
                "user_type": "individual",
                "poa": false,
                "ddpi": false,
                "is_active": true
            }
        }
    Error Codes:
        UDAPI100058	No segments for these users are active. Manual reactivation is recommended from Upstox app/web. - Thrown when the signing-in user lacks active segments on their account. It's recommended that the user re-enable these segments through the Upstox app or website.
    Java SKD:
        import com.upstox.ApiClient;
        import com.upstox.ApiException;
        import com.upstox.Configuration;
        import com.upstox.api.GetProfileResponse;
        import com.upstox.auth.*;
        import io.swagger.client.api.UserApi;
        public class Main {
            public static void main(String[] args) {
                ApiClient defaultClient = Configuration.getDefaultApiClient();

                OAuth OAUTH2 = (OAuth) defaultClient.getAuthentication("OAUTH2");
                OAUTH2.setAccessToken("{your_access_token}");

                UserApi apiInstance = new UserApi();
                String apiVersion = "2.0";
                try {
                    GetProfileResponse result = apiInstance.getProfile(apiVersion);
                    System.out.println(result);
                } catch (ApiException e) {
                    System.err.println("Exception when calling UserApi#getProfile");
                    e.printStackTrace();
                }
            }
        }
GET /user/get-funds-and-margin: V2 ---> Get Funds and Margin
    Request:
        curl --location 'https://api.upstox.com/v2/user/get-funds-and-margin' \
        --header 'Content-Type: application/json' \
        --header 'Accept: application/json' \
        --header 'Authorization: Bearer {your_access_token}'
    Response:
        {
            "status": "success",
            "data": {
                "equity": {
                // Combined funds for both Equity and Commodity segments
                },
                "commodity": {
                // All values will be set to zero
                }
            }
        }
    Query Parameters:
        Name	    Required	    Type	    Description
        segment	    false	        string	    Determines the market segment related to the.       
                                                aspecified segment in the request, the response will encompass results for both commodities and equities. Possible values: for Equity use SEC, for Commodity use COM
    Error Codes:
        UDAPI100072	The Funds service is accessible from 5:30 AM to 12:00 AM IST daily - Thrown when an funds API is called between midnight and 5:30 AM in the morning.
    Java SKD:
        import com.upstox.ApiClient;
        import com.upstox.ApiException;
        import com.upstox.Configuration;
        import com.upstox.api.GetUserFundMarginResponse;
        import com.upstox.auth.*;
        import io.swagger.client.api.UserApi;
        public class Main {
            public static void main(String[] args) {
                ApiClient defaultClient = Configuration.getDefaultApiClient();

                OAuth OAUTH2 = (OAuth) defaultClient.getAuthentication("OAUTH2");
                OAUTH2.setAccessToken("{your_access_token}");

                UserApi apiInstance = new UserApi();
                String apiVersion = "2.0"; // String | API Version Header
                String segment = ""; 
                try {
                    GetUserFundMarginResponse result = apiInstance.getUserFundMargin(apiVersion, segment);
                    System.out.println(result);
                } catch (ApiException e) {
                    System.err.println("Exception when calling UserApi#getUserFundMargin");
                    e.printStackTrace();
                }
            }
        }