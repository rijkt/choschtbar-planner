```
zip -r "getShifts-$(date +"%Y-%m-%d-%H-%M").zip" node_modules/ index.js package.json

aws lambda update-function-code --function-name getShifts --zip-file fileb://$PWD/getShifts-<date>.zip

aws lambda invoke --function-name getShifts /tmp/outfile
```
