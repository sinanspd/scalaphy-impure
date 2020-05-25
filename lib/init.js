const files = require('./files');
const chalk = require('chalk');
const inquirer = require('inquirer');

module.exports = {
    initProject: () => {
        if (files.directoryExists('.scalaphy.json')) {
            console.log(chalk.red('Already a Scalaphy repository!'));
            process.exit();
        }else{
            const questions = [ 
                {
                    name: 'Waiver',
                    type: 'list',
                    message: "Are you familiar with pure functional programming at least at a beginner level?",
                    choices: ["Yes", "No"]
                    // Need to break here somehow 
                }, 
                {
                    name: 'Org Name',
                    type: 'input',
                    message: "Enter Your Org i.e. app.vizion ",
                    validate: function(value){
                        if(value.length){
                            return true;
                        }else{
                            return "Please Enter A Valid Org Name"
                        }
                    }
                }, 
                {
                    name: 'Project Name',
                    type: 'input',
                    message: "What Do You Want The Project To Be Called?",
                    validate: function(value){
                        if(value.length){
                            return true;
                        }else{
                            return "Please Enter A Project Name"
                        }
                    }
                },
                {
                    name: 'API Type',
                    type: 'list',
                    message: "Http or GraphQL?",
                    choices: ["Http", "GraphQL"],
                },
                {
                    name: 'Features',
                    type: 'checkbox',
                    message: "Which features do you want?",
                    choices: ["Authentication", "Email", "Payments"],
                }
            ];
            return inquirer.prompt(questions);
        }
    }
}