# Laminar gRPC
To start all laminar gRPC services use below command
```sh
    make start_all
```

To run the frontend UI use below command
```sh
    make start_ui
```

For first run install frontend ui dependencies
```sh
    make install_ui
```

This connectes to the graphql server running @ http://localhost:9090/graphql, to run the same use below command
```sh
    make start_gql_server
```

The graphql server connects to the gRPC Api, to run the same use the command as below
```sh
    make start_grpc_server
```
