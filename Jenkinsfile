pipeline {
  agent {
    node {
      label 'maven'
    }

  }
   parameters {
    string(name: 'PROJECT_VERSION', defaultValue: 'v1.0', description: '')
    string(name: 'PROJECT_NAME', defaultValue: '', description: '')
  }

   environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITEE_CREDENTIAL_ID = 'gitee-id'
    KUBECONFIG_CREDENTIAL_ID = 'sangomall-kubeconfig'
    REGISTRY = 'docker.io'
    DOCKERHUB_NAMESPACE = 'nextgomsb'
    GITEE_ACCOUNT = 'nextgomsb'
    SONAR_CREDENTIAL_ID = 'sonar-qube'
  }


  stages {
    stage('拉取项目代码') {
      agent none
      steps {
        git(url: 'https://gitee.com/nextgomsb/sangomall.git', credentialsId: 'gitee-id', changelog: true, poll: false)
      }
    }

    stage('代码质量检查及分析') {
      agent none
      steps {
        container('maven') {
          withCredentials([string(credentialsId : 'sonar-qube' ,variable : 'SONAR_TOKEN' ,)]) {
            withSonarQubeEnv('sonar') {
              sh 'echo 当前目录 `pwd`'
              sh 'mvn clean install -Dmaven.test.skip=true -gs `pwd`/mvn_settings.xml'
              sh 'mvn sonar:sonar -gs `pwd`/mvn_settings.xml -Dsonar.login=$SONAR_TOKEN'
            }

          }

          timeout(unit: 'HOURS', activity: true, time: 1) {
            waitForQualityGate 'true'
          }

        }

      }
    }

    stage('单元测试') {
      agent none
      steps {
        container('maven') {
          sh 'mvn clean package -Dmaven.test.skip=true -gs `pwd`/mvn_settings.xml'
        }

      }
    }

    stage('构建项目容器镜像及推送') {
      agent none
      steps {
        container('maven') {
          sh 'mvn clean package -Dmaven.test.skip=true -gs `pwd`/mvn_settings.xml'
          sh 'cd $PROJECT_NAME && docker build -f Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BUILD_NUMBER .'
          withCredentials([usernamePassword(credentialsId : 'dockerhub-id' ,passwordVariable : 'DOCKER_PASSWORD' ,usernameVariable : 'DOCKER_USERNAME' ,)]) {
            sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
            sh 'docker push $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BUILD_NUMBER'
            sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest'
            sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest'
          }

        }

      }
    }

    stage('创建项目代码及容器镜像的发布版') {
      agent none
      when {
        expression {
          return params.PROJECT_VERSION =~ /v.*/
        }

      }
      steps {
        container('maven') {
          input(message: '''@project-admin
是否允许推送本次项目代码及容器镜像的发布版？''', submitter: 'project-admin')
          withCredentials([usernamePassword(credentialsId : 'gitee-id' ,passwordVariable : 'GITEE_PASSWORD' ,usernameVariable : 'GITEE_USERNAME' ,)]) {
            sh 'git config --global user.email "nextgo@126.com" '
            sh 'git config --global user.name "nextgo" '
            sh 'git tag -a $PROJECT_VERSION -m "$PROJECT_VERSION" '
            sh 'git push http://$GITEE_USERNAME:$GITEE_PASSWORD@gitee.com/$GITEE_ACCOUNT/sangomall.git --tags --ipv4'
          }

          sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION '
          sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION'
        }

      }
    }

    stage('部署微服务项目到K8S集群') {
      agent none
      steps {
        input(message: '''@project-admin
是否允许发布微服务项目到K8S集群？''', submitter: 'project-admin')
        container('maven') {
          withCredentials([kubeconfigContent(credentialsId : 'sangomall-kubeconfig' ,variable : 'KUBECONFIG_CONTENT' ,)]) {
            sh '''mkdir ~/.kube
echo "$KUBECONFIG_CONTENT" > ~/.kube/config
envsubst < $PROJECT_NAME/deploy/deploy.yaml | kubectl apply -f -'''
          }

        }

      }
    }

  }

}