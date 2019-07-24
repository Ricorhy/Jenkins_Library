def call(String args) {
    println "Running MavenBuild with args - ${args}"
    try {
        maven {
            exec = args
        }
        archiveArtifacts artifacts: '**/target/*.jar,**/target/*.zip', fingerprint: true
    }
    finally {

        junit allowEmptyResults: true, testResults: '**/*surefire-reports/TEST-*.xml'

        def jacocoUtReportDir = getShellOut {
            exec = "find . -type d -name jacoco-ut | sed 's+\\./++g'"
        }

        def jacocoItReportDir = getShellOut {
            exec = "find . -type d -name jacoco-it | sed 's+\\./++g'"
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
        if (fileExists("${jacocoItReportDir}/index.html")) {        
            publishHTML([
                allowMissing: true, 
                alwaysLinkToLastBuild: false, 
                keepAll: true, 
                reportDir: jacocoItReportDir, 
                reportFiles: 'index.html', 
                reportName: 'Jacoco IT Report', 
                reportTitles: ''])
        }

        if (currentBuild.result.equals('FAILURE')) {
            error 'MavenBuild failed.'
        }
    }
}