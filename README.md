# choschtbar-planner

## Run

``` shell
yarn install

yarn watch-css

yarn watch
```

## Release

``` shell
yarn clean

export REDIRECT_URI="<URL>"; yarn release

aws s3 sync public/ s3://choschtbar-planner
```

## Update Lambda
```shell
zip -r "<lambda-name>-$(date +"%Y-%m-%d-%H-%M").zip" lambda_function.py

aws lambda update-function-code --function-name <lambda-name> --zip-file fileb://$PWD/<zipFile>
```
## Test lambda
```shell
aws lambda invoke --function-name <lambda-name> /tmp/outfile

jq . /tmp/outfile
```
