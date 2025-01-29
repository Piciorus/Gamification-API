openApiGenerate.doLast {
    def resourcePath = "common-error-handling.yaml"
    def resourceStream = this.class.classLoader.getResourceAsStream(resourcePath)
    if (resourceStream == null) {
        throw new FileNotFoundException("Cannot find $resourcePath in classpath.")
    }
    println "Successfully loaded $resourcePath from commons-lib.jar"
}
