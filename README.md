# Introduction 

Simple azure function build pipeline for python and dotnet technology.


# Dotnet project setup
- Function project is created with func init azure-keyvault-encryption-demos-csharp 
- Then enter the project folder cd azure-keyvault-encryption-demos-csharp
- New http triggered function is added with func new --template "Http Trigger" --name MyHttpTrigger 
- Function is started with func start  at local
- Then you can use http://localhost:7071/api/MyHttpTrigger command to reach

# Python project setup
- Function project is created with func init azure-keyvault-encryption-demos-python 
- Then enter the project folder cd azure-keyvault-encryption-demos-python
- New http triggered function is added with func new --template "Http Trigger" --name MyHttpTriggerPython
- Function is started with func start  
- Then you can use http://localhost:7071/api/MyHttpTriggerPython command to reach

# Build and Test
- Run main azure-pipelines.yml and choose appropriate technology.Default is python

# Contribute
TODO:  Azure publish option will be add