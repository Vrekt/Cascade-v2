# Cascade-v2
A basic chat server in java!

# Configuration

Cascade has a few configuration options.

If true, Cascade will check upon a new client connecting if the clients username matches anybody elses.
```
duplicate_name_checking: true
```

If true, Cascade will check upon a new client connecting if the clients IP matches anybody elses.
```
duplicate_ip_checking: false
```

This value indicates the max length a username can be.
```
max_username_characters: 16
```

This value indicates how long a new client is allowed to be in the authentication process,
ex, entering a password.
```
authentication_timeout_seconds: 10
```

This value indicates how long before a client is timed out for not sending keep alive packets.
```
keepalive_timeout_seconds: 10
```

This value indicates how many threads Cascade will create dedicated to connecting new clients.
If the limit is reached all clients will have to wait until a connection is completed for them to be processed. 
```
max_connection_threads: 10
```

# Launching
Cascade can be launched with the following options

```
java -jar Cascade.jar <port> <auth-type(basic, password, none)> <password>(OPTIONAL) <config-file-path>
```
