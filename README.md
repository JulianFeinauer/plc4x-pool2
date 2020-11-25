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
