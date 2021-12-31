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

yarn release

yarn build-css

aws s3 sync public/ s3://choschtbar-planner
```

## Update Lambda
```shell
zip -r  "<lambda-name>-$(date +"%Y-%m-%d-%H-%M").zip" lambda_function.py

aws lambda  update-function-code --function-name <lambda-name> --zip-file fileb://$PWD/<zipFile>
```
