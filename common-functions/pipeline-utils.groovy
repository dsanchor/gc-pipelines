// pipeline-utils functions

def createConfigMap(name, pathToFiles) {
   echo "Creating configmap ${name} using files under path: ${pathToFiles}"
   // create temporal configmap and replace current one
   def configMap = openshift.create( "configmap", "${name}-tmp", "--from-file", pathToFiles ).object(exportable:true)
   configMap.metadata.name = name
   openshift.apply(configMap)  	
   openshift.selector( "configmap/${name}-tmp" ).delete()	
}

def rollout(appName) {
   echo "Deploying application ${appName}"
   def dc = openshift.selector("dc", appName)

   def replicas = dc.object().spec.replicas
   def currentPods = dc.related('pods').count()

   def rm = dc.rollout() 
   def lastDeploy = rm.latest()
   echo "${lastDeploy.out}"
    
   dc.related( 'pods' ).watch {
     // End the watch only when rolling new pods
     echo "Total number of current pods are ${it.count()} while old ones were ${currentPods}"
     return it.count() > currentPods 
   }
   echo "Rolling out deployment"
   dc.related( 'pods' ).watch {
     // End the watch only once the exact number of replicas is back
     echo "New pods are ${it.count()} and should match ${replicas}"
     return it.count() == replicas 
   }
   // Let's wait until pods are Running
   dc.related( 'pods' ).untilEach {
     echo "Pod ${it.object().metadata.name} is ${it.object().status.phase}"
     return it.object().status.phase == 'Running'
   }
   echo "New deployment ready"
}

return this
