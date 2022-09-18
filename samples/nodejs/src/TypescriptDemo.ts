import {ClientCertificateCredential} from "@azure/identity"

const tenantId = process.env['AZ_TENANT_ID']
const clientId = process.env['AZ_CLIENT_ID']
const pemPath = process.env['AZ_PEM_PATH']
const keyId = process.env['AZ_KEY_ID']
// const mongoURL = process.env['MONGO_URL']

if (tenantId && clientId && pemPath && keyId) {
    const creds = new ClientCertificateCredential(tenantId, clientId, pemPath)

    // const ccClient = new CryptographyClient(keyId, creds)
    

} else {
    console.log('Missing necessary environment variable')
    process.exit(1)
}
