import java.util.zip.ZipFile

task extractCommonErrorYaml {
    doLast {
        def jarFile = configurations.runtimeClasspath.find { it.name.contains("commons-lib") }
        if (!jarFile) {
            throw new FileNotFoundException("commons-lib.jar not found in dependencies!")
        }

        def yamlFileName = "common-error-handling.yaml"
        def outputDir = file("$buildDir/extracted-yaml")
        outputDir.mkdirs()

        def outputFile = new File(outputDir, yamlFileName)

        new ZipFile(jarFile).withCloseable { zip ->
            def entry = zip.getEntry(yamlFileName)
            if (entry == null) {
                throw new FileNotFoundException("$yamlFileName not found inside $jarFile")
            }
            zip.getInputStream(entry).withCloseable { input ->
                outputFile.bytes = input.bytes
            }
        }

        println "Extracted $yamlFileName to: ${outputFile.absolutePath}"
    }
}
