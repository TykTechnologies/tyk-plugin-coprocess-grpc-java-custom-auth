Java gRPC plugin
==

## Plugin overview

This repository provides a sample [gRPC](http://www.grpc.io/) plugin, written in Java, intended to work as part of [Tyk](https://tyk.io/). Gradle is used.

Simple auth logic is implemented, based on [Tyk custom middleware hooks](https://tyk.io/docs/tyk-api-gateway-v1-9/javascript-plugins/middleware-scripting/) logic.
A class implements the required hook methods.

## The hook

This plugin implements a single hook, it performs authorization by comparing a header value, you may see the code [here](https://github.com/TykTechnologies/tyk-plugin-coprocess-grpc-java-custom-auth/blob/master/src/main/java/com/tyktechnologies/tykmiddleware/TykDispatcher.java).

## Running the gRPC server

	gradle run

## Building a plugin bundle

The [`manifest.json`](manifest.json) file describes the hooks implemented by this plugin, to use it you must generate a bundle and load it into your Tyk API settings, [this guide](https://tyk.io/tyk-documentation/customise-tyk/plugins/rich-plugins/plugin-bundles/) will walk you through the process or a blog post [here](https://tyk.io/blog/how-to-setup-custom-authentication-middleware-using-grpc-and-java/

