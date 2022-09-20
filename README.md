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

## Develop Lambdas with CIDER
A Lambda defined as a node library can be loaded into CIDER with the following setup:
```
yarn run shadow-cljs server

yarn run shadow-cljs node-repl
```
Then run `M-x cider-connect-cljs` with host and port as defined in shadow-cljs.edn. Use REPL type 'shadow' and build id 'node-repl'. You're ready to load and import your Lambda code.

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
## Maintenance
Find outdated dependencies
```shell
yarn outdated

clojure -Tantq outdated
```