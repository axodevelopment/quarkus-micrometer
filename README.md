# quarkus-micrometer
Quarkus micrometer combo.


Enable Userworkload monitoring

Old versions

```bash
oc -n openshift-monitoring edit configmap cluster-monitoring-config
```

```bash
apiVersion: v1
kind: ConfigMap
metadata:
  name: cluster-monitoring-config
  namespace: openshift-monitoring
data:
  config.yaml: |
    enableUserWorkload: true 
```

For prom remote

```bash
oc -n openshift-user-workload-monitoring edit configmap user-workload-monitoring-config
```


---

get image stream up and running

```bash
oc patch configs.imageregistry.operator.openshift.io/cluster --type=merge -p \
'{"spec":{"managementState":"Managed","storage":{"emptyDir":{}}}}'
```

Wait for pods to come back up

```bash
oc -n openshift-image-registry get pods
```

