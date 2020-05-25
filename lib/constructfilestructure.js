const mkdirp = require('mkdirp')
const chalk = require('chalk');
const ejs = require('ejs');
const fs = require('fs');

module.exports = {
    initProject: (name, mode) => {
        mkdirp(`./${name}`).then(m =>
            constructSBTProject("hello", name, true)
        )
    }
}



function constructAuthService() {
    return 5;
}

function constructEmailService() {
    return -1;
}

function constructPaymentService() {
    return -1;
}

/**  
 * Construct the following directory structure and generate the default files
 * @param name name of the sbt project
 * @param rootPath directory to init the project in 
 * @param mode Http or GraphQL 
    ${project-name}
    | - ${nameCore}
    | - project
        | - build.properties
        | - Dependencies.scala
        | - plugins.sbt 
    .gitignore
    .scalafmt.conf
    build.sbt
    version.sbt
**/ 
function constructSBTProject(name, rootPath, mode){
  mkdirp(`./${rootPath}/${name}`).then(m => {
        console.log(chalk.green(`Created Project folder ${m}`));
        Promise.all([
                mkdirp(`./${rootPath}/${name}/${name}Core`),
                mkdirp(`./${rootPath}/${name}/project`)
        ]).then(a => {
            console.log(chalk.green(`Created Folder ${a[0]}`));
            console.log(chalk.green(`Created Folder ${a[1]}`));
            constructBuildSbt(rootPath, name)
            constructVersionSBT(rootPath, name)
            constructDependenciesSBT(rootPath, name)
            constructBuildProperties(rootPath, name)
            constructPluginsSBT(rootPath, name)
        })
    })
}

function constructBuildSbt(rootPath, name){
    ejs.renderFile('./templates/server/auth/build.sbt.ejs', {appOrg: "app.vizion"}, function(err, str){
        if(err){console.log(chalk.red(err))}
        else{
            fs.writeFile(`./${rootPath}/${name}/build.sbt`, str, function(err){
                if(err) console.log(chalk.red(err))
                else console.log(chalk.green(`Created build.sbt for project ${name}`));
            })}
    });
}

function constructVersionSBT(rootPath, name){
    ejs.renderFile('./templates/server/auth/version.sbt.ejs', {}, function(err, str){
        if(err){console.log(chalk.red(err))}
        else{
            fs.writeFile(`./${rootPath}/${name}/version.sbt`, str, function(err){
                if(err) console.log(chalk.red(err))
                else console.log(chalk.green(`Created version.sbt for project ${name}`));
            })}
    });
}

function constructDependenciesSBT(rootPath, name){
    ejs.renderFile('./templates/server/auth/Dependencies.sbt.ejs', {}, function(err, str){
        if(err){console.log(chalk.red(err))}
        else{
            fs.writeFile(`./${rootPath}/${name}/project/Dependencies.sbt`, str, function(err){
                if(err) console.log(chalk.red(err))
                else console.log(chalk.green(`Created Dependencies.sbt for project ${name}`));
            })}
    });
}

function constructBuildProperties(rootPath, name){
    ejs.renderFile('./templates/server/auth/build.properties.ejs', {}, function(err, str){
        if(err){console.log(chalk.red(err))}
        else{
            fs.writeFile(`./${rootPath}/${name}/project/build.properties`, str, function(err){
                if(err) console.log(chalk.red(err))
                else console.log(chalk.green(`Created build.properties for project ${name}`));
            })}
    });
}

function constructPluginsSBT(rootPath, name){
    ejs.renderFile('./templates/server/auth/plugins.sbt.ejs', {}, function(err, str){
        if(err){console.log(chalk.red(err))}
        else{
            fs.writeFile(`./${rootPath}/${name}/project/plugins.sbt`, str, function(err){
                if(err) console.log(chalk.red(err))
                else console.log(chalk.green(`Created plugins for project ${name}`));
            })}
    });
}