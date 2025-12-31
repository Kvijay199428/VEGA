
--------------------PART 2---------------------
Get Order History
API to retrieve the details of a specific order. Orders placed by the user remain available for one trading day and are automatically removed at the end of the trading session. Provides details regarding the progression of an order through its various execution stages. For a comprehensive list of all possible order statuses, please refer to the appendix on order status.

Order history can be retrieved by utilizing either the order_id or a tag.

When both options are provided, the response will include the history of the order that perfectly matches both the order_id and tag.
If only the tag is provided, the response will include the history of all associated orders that match the given tag.
Request
curl --location 'https://api.upstox.com/v2/order/history?order_id=240108010445130' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer {your_access_token}'

For additional samples in various languages, please refer to the Sample code section on this page.

Query Parameters
Name	Required	Type	Description
order_id	false	string	The order reference ID for which the order history is required. For the regex pattern applicable to this field, see the Field Pattern Appendix.
tag	false	string	The unique tag of the order for which the order history is being requested
Responses
200
4XX
Response Body
{
  "status": "success",
  "data": [
    {
      "exchange": "NSE",
      "price": 571.35,
      "product": "D",
      "quantity": 1,
      "status": "put order req received",
      "tag": null,
      "validity": "DAY",
      "average_price": 0.0,
      "disclosed_quantity": 0,
      "exchange_order_id": null,
      "exchange_timestamp": null,
      "instrument_token": "NSE_EQ|INE062A01020",
      "is_amo": false,
      "status_message": null,
      "order_id": "231019025564798",
      "order_request_id": "1",
      "order_type": "LIMIT",
      "parent_order_id": "NA",
      "trading_symbol": "SBIN",
      "tradingsymbol": "SBIN",
      "order_timestamp": "2023-10-19 13:25:56",
      "filled_quantity": 0,
      "transaction_type": "SELL",
      "trigger_price": 0.0,
      "placed_by": "******",
      "variety": "SIMPLE"
    },
    {
      "exchange": "NSE",
      "price": 571.35,
      "product": "D",
      "quantity": 1,
      "status": "validation pending",
      "tag": null,
      "validity": "DAY",
      "average_price": 0.0,
      "disclosed_quantity": 0,
      "exchange_order_id": null,
      "exchange_timestamp": null,
      "instrument_token": "NSE_EQ|INE062A01020",
      "is_amo": false,
      "status_message": null,
      "order_id": "231019025564798",
      "order_request_id": "1",
      "order_type": "LIMIT",
      "parent_order_id": "NA",
      "trading_symbol": "SBIN",
      "tradingsymbol": "SBIN",
      "order_timestamp": "2023-10-19 13:25:56",
      "filled_quantity": 0,
      "transaction_type": "SELL",
      "trigger_price": 0.0,
      "placed_by": "******",
      "variety": "SIMPLE"
    },
    {
      "exchange": "NSE",
      "price": 571.35,
      "product": "D",
      "quantity": 1,
      "status": "open pending",
      "tag": null,
      "validity": "DAY",
      "average_price": 0.0,
      "disclosed_quantity": 0,
      "exchange_order_id": null,
      "exchange_timestamp": null,
      "instrument_token": "NSE_EQ|INE062A01020",
      "is_amo": false,
      "status_message": null,
      "order_id": "231019025564798",
      "order_request_id": "1",
      "order_type": "LIMIT",
      "parent_order_id": "NA",
      "trading_symbol": "SBIN",
      "tradingsymbol": "SBIN",
      "order_timestamp": "2023-10-19 13:25:56",
      "filled_quantity": 0,
      "transaction_type": "SELL",
      "trigger_price": 0.0,
      "placed_by": "******",
      "variety": "SIMPLE"
    },
    {
      "exchange": "NSE",
      "price": 571.35,
      "product": "D",
      "quantity": 1,
      "status": "open",
      "tag": null,
      "validity": "DAY",
      "average_price": 0.0,
      "disclosed_quantity": 0,
      "exchange_order_id": "1300000025727177",
      "exchange_timestamp": "2023-10-19 13:25:56",
      "instrument_token": "NSE_EQ|INE062A01020",
      "is_amo": false,
      "status_message": null,
      "order_id": "231019025564798",
      "order_request_id": "1",
      "order_type": "LIMIT",
      "parent_order_id": "NA",
      "trading_symbol": "SBIN",
      "tradingsymbol": "SBIN",
      "order_timestamp": "2023-10-19 13:25:56",
      "filled_quantity": 0,
      "transaction_type": "SELL",
      "trigger_price": 0.0,
      "placed_by": "******",
      "variety": "SIMPLE"
    },
    {
      "exchange": "NSE",
      "price": 571.35,
      "product": "D",
      "quantity": 1,
      "status": "open",
      "tag": null,
      "validity": "DAY",
      "average_price": 0.0,
      "disclosed_quantity": 0,
      "exchange_order_id": "1300000025727177",
      "exchange_timestamp": "2023-10-19 13:25:56",
      "instrument_token": "NSE_EQ|INE062A01020",
      "is_amo": false,
      "status_message": null,
      "order_id": "231019025564798",
      "order_request_id": "1",
      "order_type": "LIMIT",
      "parent_order_id": "NA",
      "trading_symbol": "SBIN",
      "tradingsymbol": "SBIN",
      "order_timestamp": "2023-10-19 13:25:56",
      "filled_quantity": 1,
      "transaction_type": "SELL",
      "trigger_price": 0.0,
      "placed_by": "******",
      "variety": "SIMPLE"
    },
    {
      "exchange": "NSE",
      "price": 571.35,
      "product": "D",
      "quantity": 1,
      "status": "complete",
      "tag": null,
      "validity": "DAY",
      "average_price": 571.4,
      "disclosed_quantity": 0,
      "exchange_order_id": "1300000025727177",
      "exchange_timestamp": "2023-10-19 13:25:56",
      "instrument_token": "NSE_EQ|INE062A01020",
      "is_amo": false,
      "status_message": null,
      "order_id": "231019025564798",
      "order_request_id": "1",
      "order_type": "LIMIT",
      "parent_order_id": "NA",
      "trading_symbol": "SBIN",
      "tradingsymbol": "SBIN",
      "order_timestamp": "2023-10-19 13:25:56",
      "filled_quantity": 1,
      "transaction_type": "SELL",
      "trigger_price": 0.0,
      "placed_by": "******",
      "variety": "SIMPLE"
    }
  ]
}

Name	Type	Description
status	string	A string indicating the outcome of the request. Typically success for successful operations.
data	object	Response data for order details
data[].exchange	string	Exchange to which the order is associated. Valid exchanges can be found in the Exchange Appendix
data[].price	float	Price at which the order was placed
data[].product	string	Signifies if the order was either Intraday, Delivery or CO.
Possible values: I, D, CO, MTF.
data[].quantity	int32	Quantity with which the order was placed
data[].status	string	Indicates the current status of the order. Valid order statuses can be found in the Order Status Appendix
data[].tag	string	Tag to uniquely identify an order
data[].validity	string	It can be one of the following - DAY(default), IOC.
Possible values: DAY, IOC.
data[].average_price	float	Average price at which the qty got traded
data[].disclosed_quantity	int32	The quantity that should be disclosed in the market depth
data[].exchange_order_id	string	Unique order ID assigned by the exchange for the order placed
data[].exchange_timestamp	string	User readable time at which the order was placed or updated
data[].instrument_token	string	Key of the instrument. For the regex pattern applicable to this field, see the Field Pattern Appendix.
data[].is_amo	boolean	Signifies if the order is an After Market Order
data[].status_message	string	Indicates the reason when any order is rejected, not modified or cancelled
data[].order_id	string	Unique order ID assigned internally for the order placed
data[].order_request_id	string	Apart from 1st order it shows the count of how many requests were sent
data[].order_type	string	Type of order. It can be one of the following MARKET refers to market order LIMIT refers to Limit Order SL refers to Stop Loss Limit SL-M refers to Stop Loss Market.
Possible values: MARKET, LIMIT, SL, SL-M.
data[].parent_order_id	string	In case the order is part of the second of a CO, the parent order ID is indicated here
data[].trading_symbol	string	Shows the trading symbol of the instrument
data[].order_timestamp	string	User readable timestamp at which the order was placed
data[].filled_quantity	int32	The total quantity traded from this particular order
data[].transaction_type	string	Indicates whether its a buy or sell order.
Possible values: BUY, SELL.
data[].trigger_price	float	If the order was a stop loss order then the trigger price set is mentioned here
data[].placed_by	string	Uniquely identifies the user (commonly referred as UCC)
data[].variety	string	Order complexity

Sample Code
Get order history for an order number
Python
Node.js
Java
PHP
Python SDK
Node.js SDK
Java SDK
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String url = "https://api.upstox.com/v2/order/history";
        String accept = "application/json";
        String authorization = "Bearer {your_access_token}";
        String orderId = "240108010445130";

        try {
            HttpClient httpClient = HttpClient.newHttpClient();

            // Build the URI with query parameters
            URI uri = URI.create(url + "?order_id=" + orderId);

            // Build the request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .header("Accept", accept)
                    .header("Authorization", authorization)
                    .GET()
                    .build();

            // Send the request and get the response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Print the response
            System.out.println("Response Code: " + response.statusCode());
            System.out.println("Response: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


GET /order/history
Request
Live URL
https://api.upstox.com/v2
order_id — query
The order reference ID for which the order history is required
tag — query
The unique tag of the order for which the order history is being requested
Get Order Book
API to retrieve the list of a orders placed for the current day. Orders initiated by the user remain active for a single day and are automatically cleared at the conclusion of the trading session. The reply indicates the most current status of the order. For a comprehensive list of all possible order statuses, please refer to the appendix on order status.

Request
curl --location 'https://api.upstox.com/v2/order/retrieve-all' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer {your_access_token}'

For additional samples in various languages, please refer to the Sample code section on this page.

Responses
200
Response Body
[
  {
    "exchange": "BSE",
    "product": "D",
    "price": 0.8,
    "quantity": 1,
    "status": "complete",
    "guid": null,
    "tag": null,
    "instrument_token": "BSE_EQ|INE220J01025",
    "placed_by": "*****",
    "trading_symbol": "FCONSUMER",
    "tradingsymbol": "FCONSUMER",
    "order_type": "LIMIT",
    "validity": "DAY",
    "trigger_price": 0,
    "disclosed_quantity": 0,
    "transaction_type": "BUY",
    "average_price": 0.8,
    "filled_quantity": 1,
    "pending_quantity": 0,
    "status_message": null,
    "status_message_raw": null,
    "exchange_order_id": "1697686200000169148",
    "parent_order_id": null,
    "order_id": "231019025057849",
    "variety": "SIMPLE",
    "order_timestamp": "2023-10-19 09:23:23",
    "exchange_timestamp": "2023-10-19 09:23:23",
    "is_amo": false,
    "order_request_id": "2",
    "order_ref_id": "57744821658411"
  },
  {
    "exchange": "NFO",
    "product": "I",
    "price": 0,
    "quantity": 1500,
    "status": "rejected",
    "guid": null,
    "tag": null,
    "instrument_token": "NSE_FO|52567",
    "placed_by": "******",
    "trading_symbol": "SBIN23OCTFUT",
    "tradingsymbol": "SBIN23OCTFUT",
    "order_type": "SL-M",
    "validity": "DAY",
    "trigger_price": 570,
    "disclosed_quantity": 0,
    "transaction_type": "SELL",
    "average_price": 0,
    "filled_quantity": 0,
    "pending_quantity": 0,
    "status_message": "You need to add Rs. 138385.27 in your account to place this trade.",
    "status_message_raw": "RMS:Margin Exceeds,Required:153892.45, Available:15507.18 for entity account-****** across exchange across segment across product ",
    "exchange_order_id": "",
    "parent_order_id": null,
    "order_id": "231019025000398",
    "variety": "SIMPLE",
    "order_timestamp": "2023-10-19 09:15:00",
    "exchange_timestamp": null,
    "is_amo": false,
    "order_request_id": "1",
    "order_ref_id": "33832759664502"
  },
  {
    "exchange": "BFO",
    "product": "D",
    "price": 0.05,
    "quantity": 10,
    "status": "cancelled",
    "guid": null,
    "tag": null,
    "instrument_token": "BSE_FO|822663",
    "placed_by": "******",
    "trading_symbol": "SENSEX23O2066000PE",
    "tradingsymbol": "SENSEX23O2066000PE",
    "order_type": "LIMIT",
    "validity": "DAY",
    "trigger_price": 0,
    "disclosed_quantity": 0,
    "transaction_type": "BUY",
    "average_price": 0,
    "filled_quantity": 0,
    "pending_quantity": 10,
    "status_message": null,
    "status_message_raw": null,
    "exchange_order_id": "1697687100012002451",
    "parent_order_id": null,
    "order_id": "231019025000428",
    "variety": "SIMPLE",
    "order_timestamp": "2023-10-19 09:30:53",
    "exchange_timestamp": "2023-10-19 09:30:53",
    "is_amo": false,
    "order_request_id": "1",
    "order_ref_id": "1168356875930923"
  }
]


Name	Type	Description
status	string	A string indicating the outcome of the request. Typically success for successful operations.
data	object	Response data for order Book
data[].exchange	string	Exchange to which the order is associated. Valid exchanges can be found in the Exchange Appendix
data[].product	string	Signifies if the order was either Intraday, Delivery or CO.
Possible values: I, D, CO, MTF.
data[].price	float	Price at which the order was placed
data[].quantity	int32	Quantity with which the order was placed
data[].status	string	Indicates the current status of the order. Valid order statuses can be found in the Order Status Appendix
data[].tag	string	Tag to uniquely identify an order
data[].instrument_token	string	Key of the instrument. For the regex pattern applicable to this field, see the Field Pattern Appendix.
data[].placed_by	string	Uniquely identifies the user (commonly referred as UCC)
data[].trading_symbol	string	Shows the trading symbol of the instrument
data[].order_type	string	Type of order. It can be one of the following MARKET refers to market order LIMIT refers to Limit Order SL refers to Stop Loss Limit SL-M refers to Stop Loss Market.
Possible values: MARKET, LIMIT, SL, SL-M.
data[].validity	string	It can be one of the following - DAY(default), IOC.
Possible values: DAY, IOC.
data[].trigger_price	float	If the order was a stop loss order then the trigger price set is mentioned here
data[].disclosed_quantity	int32	The quantity that should be disclosed in the market depth
data[].transaction_type	string	Indicates whether its a buy or sell order.
Possible values: BUY, SELL.
data[].average_price	float	Average price at which the qty got traded
data[].filled_quantity	int32	The total quantity traded from this particular order
data[].pending_quantity	int32	Pending quantity to be filled
data[].status_message	string	Indicates the reason when any order is rejected, not modified or cancelled
data[].status_message_raw	string	Description of the order's status as received from RMS
data[].exchange_order_id	string	Unique order ID assigned by the exchange for the order placed
data[].parent_order_id	string	In case the order is part of the second of a CO, the parent order ID is indicated here
data[].order_id	string	Unique order ID assigned internally for the order placed
data[].variety	string	Order complexity
data[].order_timestamp	string	User readable timestamp at which the order was placed
data[].exchange_timestamp	string	User readable time at which the order was placed or updated
data[].is_amo	boolean	Signifies if the order is an After Market Order
data[].order_request_id	string	Apart from 1st order it shows the count of how many requests were sent
data[].order_ref_id	string	Uniquely identifies an order for internal usage.

Sample Code
Get all orders for the day
Python
Node.js
Java
PHP
Python SDK
Node.js SDK
Java SDK
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws Exception {
        String url = "https://api.upstox.com/v2/order/retrieve-all";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Set request headers
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Authorization", "Bearer {your_access_token}");

        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());
    }
}


GET /order/retrieve-all
Request
Live URL
https://api.upstox.com/v2
Get Trades
API to retrieve the list of all trades executed for the day. An order, initially submitted as one entity, can be executed in smaller segments based on market situation. Each of these partial executions constitutes a trade, and a single order may consist of several such trades.

Request
curl --location 'https://api.upstox.com/v2/order/trades/get-trades-for-day' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer {your_access_token}'

For additional samples in various languages, please refer to the Sample code section on this page.

Responses
200
Response Body
{
  "status": "success",
  "data": [
    {
      "exchange": "NSE",
      "product": "D",
      "trading_symbol": "GMRINFRA-EQ",
      "tradingsymbol": "GMRINFRA-EQ",
      "instrument_token": "151064324",
      "order_type": "MARKET",
      "transaction_type": "BUY",
      "quantity": 1,
      "exchange_order_id": "221013001021540",
      "order_id": "221013001021539",
      "exchange_timestamp": "03-Aug-2017 15:03:42",
      "average_price": 299.4,
      "trade_id": "50091502",
      "order_ref_id": "udapi-aqwsed14356",
      "order_timestamp": "23-Apr-2021 14:22:06"
    }
  ]
}

Name	Type	Description
status	string	A string indicating the outcome of the request. Typically success for successful operations.
data	object[]	Response data for trades
data[].exchange	string	Exchange to which the order is associated. Valid exchanges can be found in the Exchange Appendix
data[].product	string	Signifies if the order was either Intraday, Delivery or CO.
Possible values: I, D, CO, MTF.
data[].trading_symbol	string	Shows the trading symbol which could be a combination of symbol name, instrument, expiry date etc
data[].instrument_token	string	Key of the instrument. For the regex pattern applicable to this field, see the Field Pattern Appendix.
data[].order_type	string	Type of order. It can be one of the following MARKET refers to market order LIMIT refers to Limit Order SL refers to Stop Loss Limit SL-M refers to Stop Loss Market.
Possible values: MARKET, LIMIT, SL, SL-M.
data[].transaction_type	string	Indicates whether its a buy or sell order.
Possible values: BUY, SELL.
data[].quantity	int32	The total quantity traded from this particular order
data[].exchange_order_id	string	Unique order ID assigned by the exchange for the order placed
data[].order_id	string	Unique order ID assigned internally for the order placed
data[].exchange_timestamp	string	User readable time at when the trade occurred
data[].average_price	float	Price at which the traded quantity is traded
data[].trade_id	string	Trade ID generated from exchange towards traded transaction
data[].order_ref_id	string	Uniquely identifies an order for internal usage.
data[].order_timestamp	string	User readable timestamp at which the order was placed

Sample Code
Get all trades for the day
Python
Node.js
Java
PHP
Python SDK
Node.js SDK
Java SDK
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws Exception {
        String url = "https://api.upstox.com/v2/order/trades/get-trades-for-day";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Set request headers
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Authorization", "Bearer {your_access_token}");

        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());
    }
}


GET /order/trades/get-trades-for-day
Request
Live URL
https://api.upstox.com/v2
Get Order Trades
API to retrieve the list of all trades executed for a specific order.To access the trade information, you need to pass order_id.

Request
curl --location 'https://api.upstox.com/v2/order/trades?order_id=240108010445100' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer {your_access_token}'

For additional samples in various languages, please refer to the Sample code section on this page.


Query Parameters
Name	Required	Type	Description
order_id	true	string	The order ID for which the order to get order trades. For the regex pattern applicable to this field, see the Field Pattern Appendix.
Responses
200
4XX
Response Body
{
  "status": "success",
  "data": [
    {
      "exchange": "NSE",
      "product": "D",
      "trading_symbol": "GMRINFRA-EQ",
      "tradingsymbol": "GMRINFRA-EQ",
      "instrument_token": "151064324",
      "order_type": "MARKET",
      "transaction_type": "BUY",
      "quantity": 1,
      "exchange_order_id": "221013001021540",
      "order_id": "221013001021539",
      "exchange_timestamp": "03-Aug-2017 15:03:42",
      "average_price": 299.4,
      "trade_id": "50091502",
      "order_ref_id": "udapi-aqwsed14356",
      "order_timestamp": "23-Apr-2021 14:22:06"
    }
  ]
}

Name	Type	Description
status	string	A string indicating the outcome of the request. Typically success for successful operations.
data	object[]	Response data for trades
data[].exchange	string	Exchange to which the order is associated. Valid exchanges can be found in the Exchange Appendix
data[].product	string	Signifies if the order was either Intraday, Delivery or CO.
Possible values: I, D, CO, MTF.
data[].trading_symbol	string	Shows the trading symbol which could be a combination of symbol name, instrument, expiry date etc
data[].instrument_token	string	Key of the instrument. For the regex pattern applicable to this field, see the Field Pattern Appendix.
data[].order_type	string	Type of order. It can be one of the following MARKET refers to market order LIMIT refers to Limit Order SL refers to Stop Loss Limit SL-M refers to Stop Loss Market.
Possible values: MARKET, LIMIT, SL, SL-M.
data[].transaction_type	string	Indicates whether its a buy or sell order.
Possible values: BUY, SELL.
data[].quantity	int32	The total quantity traded from this particular order
data[].exchange_order_id	string	Unique order ID assigned by the exchange for the order placed
data[].order_id	string	Unique order ID assigned internally for the order placed
data[].exchange_timestamp	string	User readable time at when the trade occurred
data[].average_price	float	Price at which the traded quantity is traded
data[].trade_id	string	Trade ID generated from exchange towards traded transaction
data[].order_ref_id	string	Uniquely identifies an order for internal usage.
data[].order_timestamp	string	User readable timestamp at which the order was placed

Sample Code
A comprehensive set of examples is provided to illustrate various use cases and implementation scenarios for this API.

Code Examples
Python
Node.js
Java
PHP
Python SDK
Node.js SDK
Java SDK
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws Exception {
        String url = "https://api.upstox.com/v2/order/trades?order_id=240108010445100";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Set request headers
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Authorization", "Bearer {your_access_token}");

        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());
    }
}


GET /order/trades
Request
Live URL
https://api.upstox.com/v2
order_id — query required
The order ID for which the order to get order trades
Get Trade History
The Trade History API provides users with access to their historical trade and transaction data, allowing them to retrieve details of orders executed through Upstox platform. This API enables various use cases, including reviewing past month's trade activity, maintaining records for compliance or analysis, and other potential use case. Currently this API will give you data only for last 3 financial years.

Request
curl --location 'https://api.upstox.com/v2/charges/historical-trades?segment=EQ&start_date=2022-04-01&end_date=2023-03-31&page_number=1&page_size=100' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer {your_access_token}'


For additional samples in various languages, please refer to the Sample code section on this page.

Query Parameters
Name	Required	Type	Description
segment	false	string	Segment for which data is requested can be from the following options, If not provide, will consider all the segment.
EQ - Equity
FO - Futures and Options
COM - Commodity
CD - Currency Derivatives
MF - Mutual funds
start_date	true	string	Date from which data needs to be fetched. it should be within the last 3 financial years. Date format: YYYY-mm-dd.
end_date	true	string	Date till data needs to be fetched. it should be within the last 3 financial years. Date format: YYYY-mm-dd.
page_number	true	integer	Page number, the pages are starting from 1.
page_size	true	integer	Page size for pagination.
Responses
200
4XX
Response Body
EQ
FO
COM
CD
MF
{
  "status": "success",
  "data": [
    {
      "exchange": "NSE",
      "segment": "EQ",
      "option_type": "",
      "quantity": 1,
      "amount": 2252.35,
      "trade_id": "75217259",
      "trade_date": "2023-03-28",
      "transaction_type": "BUY",
      "scrip_name": "RELIANCE",
      "strike_price": "0.0",
      "expiry": "",
      "price": 2252.35,
      "isin": "INE002A01018",
      "symbol": "RELIANCE",
      "instrument_token": "NSE_EQ|INE002A01018"
    }
  ],
  "errors": null,
  "meta_data": {
    "page": {
        "page_number": 1,
        "page_size": 15,
        "total_records": 15,
        "total_pages": 1
    }
  }
}

Name	Type	Description
status	string	A string indicating the outcome of the request. Typically success for successful operations.
data	object[]	Response data for historical trade reports
data[].exchange	string	Exchange to which the order is associated. Valid exchanges can be found in the Exchange Appendix
data[].segment	string	Segment to which the order is associated.
Possible values: EQ, FO, CD, COM, MF.
data[].option_type	string	Option type of the option contracts. Possible values: CE, PE.
Option type is available only in case of FO and CD segment.
data[].quantity	integer	Quantity with which the order was placed.
data[].amount	float	Total amount at which order is bought/sold.
data[].trade_id	string	Trade ID generated from exchange towards traded transaction
data[].trade_date	string	The date on which the order was bought/sold
data[].transaction_type	string	Indicates whether its a buy or sell order.
Possible values: BUY, SELL.
data[].scrip_name	string	Name of the scrip traded
data[].strike_price	float	The strike price for the option.
data[].expiry	string	Expiry date (for derivatives). Data format is YYYY-mm-dd.
data[].price	float	Price at which the traded quantity is traded.
data[].isin	string	This represents the standard ISIN for stocks listed on multiple exchanges.
ISIN is available in case of EQ and MF segment.
data[].symbol	string	Shows the trading symbol of the instrument.
Symbol is available in case of EQ and FO segment.
data[].instrument_token	string	Key of the instrument. For the regex pattern applicable to this field, see the Field Pattern Appendix.
Instrument token is available in case of EQ and MF segment.
metadata	object	Meta data for historical trade data
metadata.page	object	Meta data for page.
metadata.page.page_number	integer	Page number for pagination
metadata.page.page_size	integer	Page size

Sample Code
Get trade history for equity segment
Curl
Python
Node.js
Java
PHP
Python SDK
Node.js SDK
Java SDK
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        String baseUrl = "https://api.upstox.com/v2/charges/historical-trades";
        String accessToken = "{your_access_token}";
        String segment = "EQ";
        String startDate = "2022-04-01";
        String endDate = "2023-03-31";
        int pageNumber = 1;
        int pageSize = 100;

        URI uri = URI.create(String.format("%s?segment=%s&start_date=%s&end_date=%s&page_number=%d&page_size=%d",
                baseUrl, segment, startDate, endDate, pageNumber, pageSize));
        URL url = uri.toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String responseBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println(responseBody);
            reader.close();
        } else {
            System.out.println("Error: " + responseCode + " - " + connection.getResponseMessage());
        }

        connection.disconnect();
    }
}



Get trade history for futures and options segment
Curl
Python
Node.js
Java
PHP
Python SDK
Node.js SDK
Java SDK
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        String baseUrl = "https://api.upstox.com/v2/charges/historical-trades";
        String accessToken = "{your_access_token}";
        String segment = "FO";
        String startDate = "2022-04-01";
        String endDate = "2023-03-31";
        int pageNumber = 1;
        int pageSize = 100;

        URI uri = URI.create(String.format("%s?segment=%s&start_date=%s&end_date=%s&page_number=%d&page_size=%d",
                baseUrl, segment, startDate, endDate, pageNumber, pageSize));
        URL url = uri.toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String responseBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println(responseBody);
            reader.close();
        } else {
            System.out.println("Error: " + responseCode + " - " + connection.getResponseMessage());
        }

        connection.disconnect();
    }
}



GET /charges/historical-trades
Request
Live URL
https://api.upstox.com/v2
start_date — query required
Date format: YYYY-mm-dd.
end_date — query required
Date format: YYYY-mm-dd.
page_number — query required
Page Number, the pages are starting from 1
page_size — query required
Page size for pagination.
segment — query
Segment for which data is requested can be from the following options EQ - Equity,   FO - Futures and Options,   COM  - Commodity,   CD - Currency Derivatives, MF - Mutual funds.
