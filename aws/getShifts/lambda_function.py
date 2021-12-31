import json
import boto3
from boto3.dynamodb.conditions import Key

def lambda_handler(event, context):
    dynamodb = boto3.resource('dynamodb')
    table = dynamodb.Table('shifts')
    result = table.query(KeyConditionExpression=Key('tour').eq('Waber'))
    return {
        'statusCode': 200,
        'body': json.dumps(result)
    }
