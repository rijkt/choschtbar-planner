# syntax=docker/dockerfile:1
FROM theasp/clojurescript-nodejs:shadow-cljs as builder
COPY . /app
WORKDIR /app
RUN yarn
RUN yarn build-css
RUN yarn release

FROM nginx:stable as server
RUN rm -rf /usr/share/nginx/html/*
COPY --from=builder /app/public /usr/share/nginx/html
