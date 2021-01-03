**Important** This repository is deprecated as the Code was contributed to Apache PLC4X as cached-connection: https://github.com/apache/plc4x

# plc4x-pool2
Alternative Connection Pool for PLC4X!

## Usage

Just see at `CachedDriverManager.java`:

Example:
```
PlcDriverManager manager = new PlcDriverManager();
PlcDriverManager cached = new CachedDriverManager(url, () -> manager.getConnection(url));
```

Now you can use "cached" everywhere you need the corresponding connection.
