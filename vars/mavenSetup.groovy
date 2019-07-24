def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    println "Creating settings-maven.xml in the current directory .."
    def nexusUrl = config.nexusUrl ?: "http://nexus:8081"
    def nexusCredentialsId = config.nexusCredentialsId ?: "adop-admin-credentials"
    withCredentials([
        usernamePassword(
            credentialsId: nexusCredentialsId,
            passwordVariable: 'NEXUS_USER',
            usernameVariable: 'NEXUS_PASSWORD')
            ]) 
    {

    sh """
cat > settings-maven.xml <<-EOF
<?xml version="1.0" encoding="UTF-8"?>

<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">

    <servers>
        <server>
            <id>nexus</id>
            <username>${NEXUS_USER}</username>
            <password>${NEXUS_PASSWORD}</password>
        </server>
    </servers>
    <mirrors>
        <mirror>
            <id>nexus</id>
            <mirrorOf>*</mirrorOf>
            <url>${nexusUrl}/content/groups/public/</url>
        </mirror>
    </mirrors>
    <profiles>
        <profile>
            <id>nexus</id>
            <activation>
                <activeByDefault>false</activeByDefault>
            </activation>
            <repositories>
                <repository>
                    <id>central</id>
                    <url>http://central</url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                    <snapshots>
                        <enabled>true</enabled>
                    </snapshots>
                </repository>
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <id>central</id>
                    <url>http://central</url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                    <snapshots>
                        <enabled>true</enabled>
                    </snapshots>
                </pluginRepository>
            </pluginRepositories>
        </profile>
        <profile>
            <id>adobe-public</id>
        
            <activation>
                <activeByDefault>false</activeByDefault>
            </activation>
        
            <properties>
        
                <releaseRepository-Id>adobe-public-releases</releaseRepository-Id>
                <releaseRepository-Name>Adobe Public
                    Releases</releaseRepository-Name>
        
                <releaseRepository-URL>http://repo.adobe.com/nexus/content/groups/public</releaseRepository-URL>
            </properties>
        
            <repositories>
                <repository>
                    <id>adobe-public-releases</id>
                    <name>Adobe Basel Public Repository</name>
                    <url>http://repo.adobe.com/nexus/content/groups/public</url>
                    <releases>
                        <enabled>true</enabled>
                        <updatePolicy>never</updatePolicy>
                    </releases>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                </repository>
            </repositories>
        
            <pluginRepositories>
                <pluginRepository>
                    <id>adobe-public-releases</id>
                    <name>Adobe Basel Public Repository</name>
                    <url>http://repo.adobe.com/nexus/content/groups/public</url>
                    <releases>
                        <enabled>true</enabled>
                        <updatePolicy>never</updatePolicy>
                    </releases>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                </pluginRepository>
            </pluginRepositories>
        </profile>
    </profiles>
</settings>
EOF
    """
    }
}