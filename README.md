# killbill-adyen-plugin

Plugin to use [Adyen](https://www.adyen.com/) as a gateway.

A full end-to-end integration demo is available [here](https://github.com/killbill/killbill-stripe-demo).

## Kill Bill compatibility

| Plugin version | Kill Bill version  | Adyen sdk version                                         | Checkout API Version|
| -------------: | -----------------: | --------------------------------------------------------: |-------------------- |
| 1.x.y          | 0.22.z             | 17.3.0 [2022-04-07](https://github.com/Adyen/adyen-java-api-library) |Version 68|



## Requirements

The plugin needs a database. The latest version of the schema can be found (killbill-adyen-plugin\src\main\resources\ddl.sql).

## Installation

Locally:

```
kpm install_java_plugin adyen --from-source-file target/adyen-plugin-*-SNAPSHOT.jar --destination /var/tmp/bundles
```

## Configuration

Go to https://ca-test.adyen.com/ca/ca/config/api_credentials_new.shtml and copy your `API key`.
Go to https://ca-test.adyen.com/ca/ca/config/showthirdparty.shtml and copy your `HMAC Key` and set your `Return Url` to http://127.0.0.1:8080/plugins/adyen-plugin/notification

Then, go to the Kaui plugin configuration page (`/admin_tenants/1?active_tab=PluginConfig`), and configure the `adyen-plugin` plugin with your key:

```java
org.killbill.billing.plugin.adyen.apiKey=test_XXX
org.killbill.billing.plugin.adyen.returnUrl=test_XXX
org.killbill.billing.plugin.adyen.merchantAccount=test_XXX
org.killbill.billing.plugin.adyen.hcmaKey=test_XXX
org.killbill.billing.plugin.adyen.enviroment= (TEST/LIVE) default is TEST
org.killbill.billing.plugin.adyen.captureDelayHours=XX (Desire capture delay in hours after Authorize , number must be between 0 - 168 hr) 
```

Alternatively, you can upload the configuration directly:

```bash
curl -v \
     -X POST \
     -u admin:password \
     -H 'X-Killbill-ApiKey: bob' \
     -H 'X-Killbill-ApiSecret: lazar' \
     -H 'X-Killbill-CreatedBy: admin' \
     -H 'Content-Type: text/plain' \
     -d 'org.killbill.billing.plugin.adyen.apiKey=test_XXX
org.killbill.billing.plugin.adyen.returnUrl=test_XXX
org.killbill.billing.plugin.adyen.merchantAccount=test_XXX
org.killbill.billing.plugin.adyen.hcmaKey=test_XXX
org.killbill.billing.plugin.adyen.captureDelayHours=XX ' \
     http://127.0.0.1:8080/1.0/kb/tenants/uploadPluginConfig/adyen-plugin
```

## Payment Method flow

The plugin create the first payment via servlet using `/sessions` [here](https://docs.adyen.com/online-payments/web-drop-in#create-payment-session) If the payment is recurring we store the token generate by Adyen an then can be used multiples time it on `/payments` [here] (https://docs.adyen.com/online-payments/tokenization/create-and-use-tokens#pay-one-off).

## Using Adyen Checkout

This plugin implementation is using Drop-in integration [here](https://docs.adyen.com/online-payments/web-drop-in)

1. Create a Kill Bill account and Kill Bill Payment (as a PluginProperty need to be sended enableRecurring = true if the payment is going to be recurring , if not can sent enableRecurring=false or simply ignore it ( by default is false)).
```
curl -v \
    -X POST \
    -u admin:password \
    -H "X-Killbill-ApiKey: bob" \
    -H "X-Killbill-ApiSecret: lazar" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -H "X-Killbill-CreatedBy: demo" \
    -H "X-Killbill-Reason: demo" \
    -H "X-Killbill-Comment: demo" \
    -d '{ "accountId": "2ad52f53-85ae-408a-9879-32a7e59dd03d", "pluginName": "adyen-plugin" ,"isDefault": true, "pluginInfo": { "isDefaultPaymentMethod": true, "properties": [ { "key": "enableRecurring", "value": "true", "isUpdatable": false } }' \
    "http://127.0.0.1:8080/1.0/kb/accounts/8785164f-b5d7-4da1-9495-33f5105e8d80/paymentMethods" 
```
2. Call `/plugins/adyen-plugin/checkout` to generate a Session where the amount need to have the decimals point if the currency have it:
```bash
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey: bob" \
     -H "X-Killbill-ApiSecret: lazar" \
     -H "Content-Type: application/json" \
     -H "Accept: application/json" \
     -H "X-Killbill-CreatedBy: demo" \
     -H "X-Killbill-Reason: demo" \
     -H "X-Killbill-Comment: demo" \
     "http://127.0.0.1:8080/plugins/adyen-plugin/checkout?kbAccountId=<KB_ACCOUNT_ID>&amount=<amount?&kbPaymentMethodId=<KB_PAYMENT_METHOD_ID>"
```

3. Redirect the user to the Adyen checkout page. The `sessionId` and `sessionData` are returned as part of the `formFields` (`id` key):





## Development

For testing you need to add your Stripe public and private key to `src/test/resources/stripe.properties`:

```
org.killbill.billing.plugin.stripe.apiKey=sk_test_XXX
org.killbill.billing.plugin.stripe.publicKey=pk_test_XXX
```

## About

Kill Bill is the leading Open-Source Subscription Billing & Payments Platform. For more information about the project, go to https://killbill.io/.