const mkdirp = require('mkdirp')
const chalk = require('chalk');
const ejs = require('ejs');
const fs = require('fs');
const files = require('./files')

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
                mkdirp(`./${rootPath}/${name}/${name}Core/src`).then(a => Promise.all([mkdirp(`./${rootPath}/${name}/${name}Core/src/main/scala`), mkdirp(`./${rootPath}/${name}/${name}Core/src/test/scala`)])),
                mkdirp(`./${rootPath}/${name}/project`)
        ]).then(a => {
            console.log(chalk.green(`Created Folder ${a[0]}`));
            console.log(chalk.green(`Created Folder ${a[1]}`));
            let rm = `./${rootPath}/${name}/`
            let templatebase = "./templates/server/auth/"
            let qs = ["build.sbt", "version.sbt", "project/Dependencies.sbt", "project/build.properties", "project/plugins.sbt"].map(a  => rm + a)
            let xs = ["build.sbt.ejs", "version.sbt.ejs", "Dependencies.sbt.ejs", "build.properties.ejs", "plugins.sbt.ejs"].map(a  => templatebase + a)
            let d = [{appOrg: "\"app.vizion\""}, {}, {}, {}, {}]
            files.renderAndWriteBatch(qs, xs, d)
        })
    })
}