# Introduction 
JNN is a cluster of projects geared towards getting the complicated parts of neural-net binary classifiers encapsulated in an easy-to-use set of tools.

# Getting Started
JNN is a Java 17 application that uses Maven for build and dependency management. You will need:
1.	A Java Development Kit -- we recommend Adoptium.
2.	Apache Maven -- many IDEs already bundle Maven.

## Build and Test
To build and test JNN, ensure that both Java and Maven are installed and on your path, and type:
```
mvn clean install
``` 
If you want to build the full project site, use:
```
mvn clean install site
``` 
## Running
To start the JavaFX training UI:
```
java -jar justneuralnets-ui/target/justneuralnets-ui-1.0-SNAPSHOT-bin.jar
```
When a model completes training, the user is prompted to save two files. The first is given a `.jnn` suffix, and contains the structure of the model. The second is given a `.mdl` suffix, and contains the trained model's parameters. Both are needed at evaluation time.

To start the evaluation API microservice:
```
java -jar jnnevalmicroservice/target/justneuralnets-evalmicroservice-1.0-SNAPSHOT-bin.jar [port] [path.jnn] [path.mdl]
```
Where `port` is the TCP port on which to listen for connections, `path.jnn` is the path to the model structure file, and `path.mdl` is the path to the model parameters file. Once started, the service offers a rudimentary "high-striker" UI for submitting features to the model for prediction.

# Contribute
As always, the code needs more tests, documentation, and user feedback. Feel free to contribute!

-------------------
# Introduction
JNN est un groupe de projets visant à encapsuler les parties complexes des classificateurs binaires de réseaux neuronaux dans un ensemble d'outils faciles à utiliser.

# Commencer
JNN est une application Java 17 qui utilise Maven pour la gestion des builds et des dépendances. Tu auras besoin de:
1. Un kit de développement Java -- nous recommandons Adoptium.
2. Apache Maven - de nombreux IDE integrent déjà Maven.

## Construire et tester
Pour créer et tester JNN, assurez-vous que Java et Maven sont installés et sur votre chemin (path), puis tapez :
```
mvn clean build
```
Si vous souhaitez créer le site complet du projet, utilisez :
```
mvn clean build site
```
## Fonctionnement
Pour démarrer l'interface utilisateur de formation JavaFX :
```
java -jar justneuralnets-ui/target/justneuralnets-ui-1.0-SNAPSHOT-bin.jar
```
Lorsqu'un modèle termine la formation, l'utilisateur est invité à enregistrer deux fichiers. Le premier est doté d'un suffixe `.jnn` et contient la structure du modèle. Le second reçoit un suffixe `.mdl` et contient les paramètres du modèle formé. Les deux sont nécessaires au moment de l'évaluation.

Pour démarrer le microservice de l'API d'évaluation :
```
java -jar jnnevalmicroservice/target/justneuralnets-evalmicroservice-1.0-SNAPSHOT-bin.jar [port] [chemin.jnn] [chemin.mdl]
```
Où `port` est le port TCP sur lequel écouter les connexions, `chemin.jnn` est le chemin d'accès au fichier de structure du modèle et `chemin.mdl` est le chemin d'accès au fichier de paramètres du modèle. Une fois démarré, le service présente une interface utilisateur rudimentaire « high-strike » pour soumettre des paramètres au modèle pour la prédiction.

# Contribuer
Comme toujours, le code a besoin de plus de tests, de documentation et de commentaires des utilisateurs. N'hésitez pas à contribuer !