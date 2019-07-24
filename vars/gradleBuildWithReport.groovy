def call(String args) {
    try {
        
        gradle {
            exec = args
        }

        archiveArtifacts artifacts: '**/build/libs/*.jar,**/build/libs/*.war,**/build/libs/*.zip', fingerprint: true
    }
    finally {

        junit allowEmptyResults: true, testResults: '**/test-results/test/*.xml'

        def jacocoUtReportDir = getShellOut {
            exec = """
            find . -type f -name *.html | grep "jacoco/test/html/index.html\$" || echo "no file pattern with jacoco/test/html/index.html were found."
            """
        }

        if (fileExists("${jacocoUtReportDir}/index.html")) {
            publishHTML([
                allowMissing: true, 
                alwaysLinkToLastBuild: false, 
                keepAll: true, 
                reportDir: jacocoUtReportDir, 
                reportFiles: 'index.html', 
                reportName: 'Jacoco UT Report', 
                reportTitles: ''])
        }

        if (currentBuild.result.equals('FAILURE'))  {
            error "gradleBuildWithReport has failed"
        }
    }
}