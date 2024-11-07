#!/usr/bin/env groovy
import groovy.json.*
import org.yaml.snakeyaml.Yaml

//  command used in testing 
//  docker run --name groov -e env=prod --rm -v "$PWD":/home/groovy/scripts -w /home/groovy/scripts groovy groovy a.groovy

// Add at the beginning
def yamlFile = System.getenv('YAML_FILE') ?: 'out.yaml'
def limitsFile = System.getenv('LIMITS_FILE') ?: 'resource/limits.json'

// Add better error handling
try {
    resourceLimits = jsonLoad.parseText((limitsFile as File).text)
} catch (Exception e) {
    println "Error loading resource limits file: ${e.message}"
    System.exit(1)
}

//load resource json using environment variable (change for pipeline)
def jsonLoad = new JsonSlurper()

// Load yaml file
Yaml parser = new Yaml()

def Map values = [
    chart_totals: [
        limit_cpu: 0,
        limit_memory:0,
        request_cpu:0,
        request_memory:0
    ],
    deployments: [:]
]

def docs = parser.loadAll((yamlFile as File).text) as Object

for (Object dep : docs) {
    if (dep.kind == 'Deployment') {
        def replica = dep.spec.replicas

        def namespace = dep.metadata.namespace

        if ( !values.deployments[namespace] ) {
            values.deployments[namespace] = [
                    namespace : namespace,
                    totals: [
                    limit_cpu:0,
                    limit_memory:0,
                    request_cpu:0,
                    request_memory:0
                    ],
                     containers:[
                    limit_cpu:0,
                    limit_memory:0,
                    request_cpu:0,
                    request_memory:0
                     ],
                     initContainers:[
                     limit_cpu:0,
                    limit_memory:0,
                    request_cpu:0,
                    request_memory:0
                     ],sidecars:[
                    limit_cpu:0,
                    limit_memory:0,
                    request_cpu:0,
                    request_memory:0
                     ]]
        }

        // these
        // containers
        for (Object cont : dep.spec.template.spec.containers) {
            addRes(namespace, cont, values, "containers", replica)
        }
        // init containers
        for (Object cont : dep.spec.template.spec.initContainers) {
                        addRes(namespace, cont, values, "initContainers", replica)
        }
        
        addResSidecar(namespace,dep , values, "sidecars")
    }   
    values.chart_totals.limit_cpu = values.chart_totals.limit_cpu.trunc(2)
}


// compare namespace values
// this
values.deployments.each { key, val ->
    compareDeploymentValues(values, resourceLimits, key,"totals")
    compareDeploymentValues(values, resourceLimits,key,"containers")
    compareDeploymentValues(values, resourceLimits,key,"initContainers")
    compareDeploymentValues(values, resourceLimits,key,"sidecars")
}

// compare total values

//total chart cpu
if(values.chart_totals.limit_cpu > resourceLimits.chart_totals.limit_cpu){
throw new Exception("total 'limit_cpu' exceeds chart resource limit : ${values.chart_totals.limit_cpu} (${resourceLimits.chart_totals.limit_cpu})")
}
//total chart memory
if(values.chart_totals.limit_memory > resourceLimits.chart_totals.limit_memory){
throw new Exception("total 'limit_memory' exceeds chart resource limit: ${values.chart_totals.limit_memory} (${resourceLimits.chart_totals.limit_memory})")
}
//total chart cpu
if(values.chart_totals.request_cpu > resourceLimits.chart_totals.request_cpu){
throw new Exception("total 'request_cpu' exceeds chart resource limit : ${values.chart_totals.request_cpu} (${resourceLimits.chart_totals.request_cpu})")
}
//total chart memory
if(values.chart_totals.request_memory > resourceLimits.chart_totals.request_memory){
throw new Exception("total 'request_memory' exceeds chart resource limit : ${values.chart_totals.request_memory} (${resourceLimits.chart_totals.request_memory})")
}



def compareDeploymentValues(values, resourceLimits, key,type){

    if(!resourceLimits.deployments.containsKey(key)){
        throw new Exception("namespace not found in resource list : ${key}")
    }

try{
//limit cpu
if(values.deployments[key][type].limit_cpu > resourceLimits.deployments[key][type].limit_cpu){
throw new Exception("total 'limit_cpu' exceeds chart resource limit : ${values.deployments[key][type].limit_cpu} (${resourceLimits.deployments[key][type].limit_cpu})")
}
//limit memory
if(values.deployments[key][type].limit_memory > resourceLimits.deployments[key][type].limit_memory){
throw new Exception("total 'limit_memory' exceeds chart resource limit: ${values.deployments[key][type].limit_memory} (${resourceLimits.deployments[key][type].limit_memory})")
}
//request cpu
if(values.deployments[key][type].request_cpu > resourceLimits.deployments[key][type].request_cpu){
throw new Exception("total 'request_cpu' exceeds chart resource limit : ${values.deployments[key][type].request_cpu} (${resourceLimits.deployments[key][type].request_cpu})")
}
//limit memory
if(values.deployments[key][type].request_memory > resourceLimits.deployments[key][type].request_memory){
throw new Exception("total 'request_memory' exceeds chart resource limit : ${values.deployments[key][type].request_memory} (${resourceLimits.deployments[key][type].request_memory})")
}
}catch(err){
    throw new Exception("unexpected error")
}
}

def addRes(namespace, cont, values, type, replica){
    values.deployments[namespace][type].limit_cpu += (returnCPU(cont.resources.limits.cpu) * replica )
    values.deployments[namespace][type].limit_memory += (returnMemory(cont.resources.limits.memory) * replica )
    values.deployments[namespace][type].request_cpu += (returnCPU(cont.resources.requests.cpu) * replica )
    values.deployments[namespace][type].request_memory  += (returnMemory(cont.resources.requests.memory) * replica )

    values.deployments[namespace]["totals"].limit_cpu += values.deployments[namespace][type].limit_cpu
    values.deployments[namespace]["totals"].limit_memory += values.deployments[namespace][type].limit_memory
    values.deployments[namespace]["totals"].request_cpu += values.deployments[namespace][type].request_cpu
    values.deployments[namespace]["totals"].request_memory  += values.deployments[namespace][type].request_memory

    values.chart_totals.limit_cpu += values.deployments[namespace][type].limit_cpu
    values.chart_totals.limit_memory += values.deployments[namespace][type].limit_memory
    values.chart_totals.request_cpu += values.deployments[namespace][type].request_cpu
    values.chart_totals.request_memory  += values.deployments[namespace][type].request_memory
}   


def addResSidecar(namespace,dep, values, type){
    try{
    values.deployments[namespace][type].limit_cpu += (returnCPU(dep.spec.template.metadata.annotations["sidecar.istio.io/proxyCPU"]) * dep.spec.replicas)
    values.deployments[namespace][type].limit_memory += (returnMemory(dep.spec.template.metadata.annotations["sidecar.istio.io/proxyMemory"])* dep.spec.replicas)
    values.deployments[namespace][type].request_cpu += (returnCPU(dep.spec.template.metadata.annotations["sidecar.istio.io/proxyCPULimit"])* dep.spec.replicas)
    values.deployments[namespace][type].request_memory  += (returnMemory(dep.spec.template.metadata.annotations["sidecar.istio.io/proxyMemoryLimit"])* dep.spec.replicas)

    values.deployments[namespace]["totals"].limit_cpu += values.deployments[namespace][type].limit_cpu
    values.deployments[namespace]["totals"].limit_memory += values.deployments[namespace][type].limit_memory
    values.deployments[namespace]["totals"].request_cpu += values.deployments[namespace][type].request_cpu
    values.deployments[namespace]["totals"].request_memory  += values.deployments[namespace][type].request_memory

    values.chart_totals.limit_cpu += values.deployments[namespace][type].limit_cpu
    values.chart_totals.limit_memory += values.deployments[namespace][type].limit_memory
    values.chart_totals.request_cpu += values.deployments[namespace][type].request_cpu
    values.chart_totals.request_memory  += values.deployments[namespace][type].request_memory
    }catch(err){

    }
}

def returnMemory(mem) {
    switch (true)
    {
        case (mem.toLowerCase().indexOf('mi') != -1):
            return Integer.parseInt(mem.toLowerCase().replaceAll('mi', ''))
            break
        case (mem.toLowerCase().indexOf('gi') != -1):
            return Integer.parseInt(mem.toLowerCase().replaceAll('gi', '')) * 1024
            break
        default:
            return Integer.parseInt(mem)
            break
    }
}

def returnCPU(cpu) {
    if (cpu.toLowerCase().indexOf('m') != -1) {
        return Float.parseFloat(cpu.toLowerCase().replaceAll('m', '')) / 1000
    }
     else {
        return Integer.parseInt(cpu)
     }
}