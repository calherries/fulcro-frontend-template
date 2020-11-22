# Copied from here: https://github.com/uosl/dataseek/blob/master/Makefile

PATH := node_modules/.bin:$(PATH)

dev: css
	clojure -A:dev -m figwheel.main -b dev

repl: css
	clojure -A:dev -A:repl

test:
	clojure -A:test

prod: export NODE_ENV := production
prod: css
	clojure -A:dev -m --build-once prod

node_modules/.bin/tailwindcss:
	npm install

resources/public/css/main.css: node_modules/.bin/tailwindcss
	tailwindcss build src/main.css -o resources/public/css/main.css

css: node_modules/.bin/tailwindcss resources/public/css/main.css

.PHONY: dev repl test prod css
