# 128bit AES GCM database encryption using Azure KeyVault and Cosmos DB

This code is written for the educational purpose to illustrate how to
implement 128-bit authenticated AES encryption in Galois Counter Mode
(GCM). The benefit of this approach is that it both encrypts the sensitive
data in (CosmosDB) database and locks the encrypted data to the database key.

The code fragments here illustrates one of the possibilities how to achieve
the compatibility between Go/Java and NodeJS implementations. Note that 
the presented solution is based around specific combination of cipher, padding, and
key length. Other combination could be possible and, most likely, will
require slight changes to the API calls.

The code in this repository can be run with unit and integration tests, however it
is not mean to run on its own.

> This code base is shared as a support material for  the Sytac talks and 
> presentations. It is not a self-standing piece. Running the code requires provisioning
> the Azure cloud infrastructure with the specific configuration.

I love to hear your feedback on which demo you would like to see! Please pitch your 
questions/requests/proposals to the email address presented to you during the
talk/presentation. Hope to see it coming in soon!

And until then, enjoy encrypting!

--
lspwd2

