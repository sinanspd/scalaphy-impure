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
                },
                {
                    name: 'Schema',
                    type: 'list',
                    message: "Add Schema Now?",
                    choices: ["Yes", "No"],
                },
                {
                    name: 'Schema 1  Name',
                    type: 'input',
                    message: "Schema Name",
                    validate: function(value){
                        if(value.length){
                            return true;
                        }else{
                            return "Please Enter A Project Name"
                        }
                    }
                },
                {
                    name: 'Field 1 Name',
                    type: 'input',
                    message: "Field Name",
                    validate: function(value){
                        if(value.length){
                            return true;
                        }else{
                            return "Please Enter A Project Name"
                        }
                    }
                },
                {
                    name: 'Field 1 Type',
                    type: 'list',
                    message: "Field Type",
                    choices: ["String", "Int", "Enum"],
                },
                {
                    name: 'More',
                    type: 'list',
                    message: "Add Another Field",
                    choices: ["Yes", "No"],
                },
                {
                    name: 'Field 2 Name',
                    type: 'input',
                    message: "Field Name",
                    validate: function(value){
                        if(value.length){
                            return true;
                        }else{
                            return "Please Enter A Project Name"
                        }
                    }
                },
                {
                    name: 'Field 2 Type',
                    type: 'list',
                    message: "Field Type",
                    choices: ["String", "Int", "Enum"],
                },
                {
                    name: 'More2',
                    type: 'list',
                    message: "Add Another Field?",
                    choices: ["Yes", "No"],
                },
                {
                    name: 'Schema 2',
                    type: 'list',
                    message: "Add Another Schema Now?",
                    choices: ["Yes", "No"],
                },
                {
                    name: 'Schema 2  Name',
                    type: 'input',
                    message: "Schema Name",
                    validate: function(value){
                        if(value.length){
                            return true;
                        }else{
                            return "Please Enter A Project Name"
                        }
                    }
                },
                {
                    name: 'Field 2.1 Name',
                    type: 'input',
                    message: "Field Name",
                    validate: function(value){
                        if(value.length){
                            return true;
                        }else{
                            return "Please Enter A Project Name"
                        }
                    }
                },
                {
                    name: 'Field 2.1 Type',
                    type: 'list',
                    message: "Field Type",
                    choices: ["String", "Int", "Enum"],
                },
                {
                    name: 'More3',
                    type: 'list',
                    message: "Add Another Field?",
                    choices: ["Yes", "No"],
                },
                {
                    name: 'Schema 3',
                    type: 'list',
                    message: "Add Another Schema Now?",
                    choices: ["Yes", "No"],
                },

            ];
            return inquirer.prompt(questions);
        }
    }
}