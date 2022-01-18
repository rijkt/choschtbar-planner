import {S3Client, SelectObjectContentCommand} from "@aws-sdk/client-s3"; // https://docs.aws.amazon.com/AWSJavaScriptSDK/v3/latest/clients/client-s3/index.html

export const handler = async () => {
    const client = new S3Client({ region: "eu-central-1" });
    const input = { // SelectObjectContentCommandInput
            Bucket: 'choschtbar-data',
            Key: 'db.json',
            ExpressionType: 'SQL',
            Expression: 'SELECT * FROM S3Object s',
            InputSerialization: {
                JSON: {
                    Type: 'DOCUMENT'
                }
            },
            OutputSerialization: {
                JSON: {
                   RecordDelimiter: '\n'
                }
            }
    };
    const selectCommand = new SelectObjectContentCommand(input);
    const selectResponse = await client.send(selectCommand);
    const events = selectResponse.Payload;
    let responseBuffer = "";
    for await (const event of events) {
        if (event.Records) {
            const record = event.Records.Payload; // Uint8Array
            responseBuffer += String.fromCharCode.apply(null, record);
        }
    }
    const response = {
        statusCode: 200,
        body: JSON.parse(responseBuffer)
    };
    return response;
};
