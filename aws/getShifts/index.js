import {S3Client, SelectObjectContentCommand} from "@aws-sdk/client-s3"; // https://docs.aws.amazon.com/AWSJavaScriptSDK/v3/latest/clients/client-s3/index.html
import {toUtf8} from "@aws-sdk/util-utf8-node";

export const handler = async (event, context) => {
    const client = new S3Client({ region: "eu-central-1" });
    const input = { // SelectObjectContentCommandInput
            Bucket: 'choschtbar-data',
            Key: 'db.json',
            ExpressionType: 'SQL',
            Expression: 'SELECT shifts FROM S3Object[*].shifts', // since this is one object we can't filter here
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
            responseBuffer += toUtf8(record);
        }
    }
    const parsed =  JSON.parse(responseBuffer);
    const shifts = Object.values(parsed.shifts); // { [id]: shift }
    const month = event.queryStringParameters?.month;
    const body = shifts.filter(item => item) // filter out null values and flatten
	  .filter(item => month ? item.month === month : item);
    const response = {
        statusCode: 200,
        headers: {
            'Access-Control-Allow-Origin': '*',
        },
        body: JSON.stringify(body)
    };
    return response;
};
