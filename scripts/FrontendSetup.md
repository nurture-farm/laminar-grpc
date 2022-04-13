#Laminar gRPC Frontend UI

Create a new project for react using create react app generator
```shell script
npx create-react-app frontend
yarn add ka-table
yarn add node-sass
```

Add relay
```shell script
yarn add react-relay
yarn add babel-plugin-macros 
yarn add --dev relay-config relay-compiler babel-plugin-relay graphql
```

Install watchman for monitoring changes of files for relay compiler
```shell script
brew update
brew install watchman
```

Install graphql schema downloader
```shell script
yarn global add get-graphql-schema
```

Download schema from server
```shell script
get-graphql-schema http://localhost:9090/graphql > src/schema.graphql
```

Setup tailwind
https://daveceddia.com/tailwind-create-react-app/

```shell script
yarn add tailwindcss npm-run-all chokidar-cli
```

Add to package.json following script
```shell script
  "build:tailwind": "tailwind build src/tailwind.css -o src/tailwind.output.css",
  "watch:tailwind": "chokidar 'src/**/*.css' 'src/**/*.scss' --ignore src/tailwind.output.css -c 'npm run build:tailwind'",
  "start": "npm-run-all build:tailwind --parallel watch:tailwind start:react",
  "start:react": "react-scripts start",
  "prebuild": "run-s build:tailwind",
  "build": "react-scripts build",
  "test": "react-scripts test",
  "eject": "react-scripts eject"
```

