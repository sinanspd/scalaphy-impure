const mkdirp = require('mkdirp')
const chalk = require('chalk');

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
    return 5;
}

function constructPaymentService() {
    return 5;
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
        ]).then(a => console.log("ok"))
    })
}

function constructBuildSbt()