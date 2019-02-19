# Project JSON to DTO

This is a personal project to test eclipse plugin development.

The plugin provides automatic POJOs generation from json information given.
Developer can either use json text on the widget page or select json file from browser.

The advantage of this plugin is that this supports hierachy of the json.
For example, if json has multiple hierachy, the plugin check all the depth and will write POJOs.

## Getting Started

Download .jar file from this project or download all these project source and export plugin jar with it.

### Prerequisites
Java 8 : This plugin is based on Java 8 environment, which means that the plugin user should work with higher version of Java in their Eclipse development environment.

### Installing

1. Add exported plugin folder in your eclipse plugin folder.
2. Restart your eclipse

## Running the tests

1. Create new java project
2. Choose one of the resource in java project and hit right mouse button
3. Select New > Other > 'Generate DTO from Json'
4. Input destination package where new POJOs will be
5. Select one of the option for source json information 
  A. If selecting json source, hit browse and find the path for the json file
  B. if selecting json text, just input whole json text in the textarea
6. Click next
7. Provide root POJO name. ex) MyBusinessDTO
8. Hit finish

## Contributing


## Versioning



## Authors

* **Sangman Na** - *Initial work* - [sangmanl](https://github.com/sangmanla)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone whose code was used
* Inspiration
* etc
