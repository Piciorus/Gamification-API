```
private Schema<?> buildSchema(List<T> codes) {
    T first = codes.get(0);

    return new ObjectSchema()
        .addProperty("code",    new StringSchema()
            .example(getErrorCode(first)))
        .addProperty("detail",  new StringSchema()
            .example(getMessage(first)))
        .addProperty("errors",  new ArraySchema()
            .items(new StringSchema())
            .example(List.of()))
        .addProperty("status",  new StringSchema()
            .example(String.valueOf(getHttpStatus(first).value())))
        .addProperty("title",   new StringSchema()
            .example(getName(first)))                      // use enum name, never null
        .addProperty("traceId", new StringSchema()
            .example("c929594d-4142-4e0b-a63d-87f920137623")); // real UUID format
}
```
