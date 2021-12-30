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
