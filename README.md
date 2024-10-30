# Curren-C
## Project Overview
This is a small project to experiment with building an Android app solely with ChatGPT-o1-preview (and a bit of Gemini, since it is currently included in Android Studio for free).
The main goal of the project is to see how hard or easy it is to build a simple app with an LLM compared to learning how to do it. I also will write down learnings from this experiment.
I used the [Android Basics with Compose Course](https://developer.android.com/courses/android-basics-compose/course) alongside sometimes, to atleast understand to some degree what I was using or doing, and also to understand how much effort it would be, to learn the steps and apply them. I'll write the app on my own then, using that course.

## App overview
The app shall be a minimal and easy to use app for converting currencies on the go. 
The app shall have three input fields which automatically update whenever you type in them and query an API (I used the [exchangerate-api](https://www.exchangerate-api.com/docs/java-currency-api)) once a week to get the newest conversion rates and using cached ones otherwise. Currency codes json gladly taken from [here](https://github.com/ourworldincode/currency/tree/main)

## Learnings so far
- I gave detailed instructions and prompted to ask clarifying questions first before starting to code. ChatGPT-o1 obeys and asks good questions to clarify. I will use this approach in further projects too.
- ChatGPT-o1 is good at providing a project outline as well, and told me what to write into which file (~20 files) in one single answer. Even the layout .xml looked good on the first try.
- However, it did not perform good on adding dependencies, especially when prompted to use a [version libray](https://developer.android.com/build/dependencies#add-dependency). It seemed to sometimes get confused between Groovy and Kotlin. In the end I had to add them manually, a painful thing and I imagine it could be a dealbreaker for people who don't know anything about coding.
- I had to let it fix several things: dependencies, permissions, (add)logging, json parsing
- ChatGPT-o1 often assumed that I know where to put the code it suggests, saying things like "in MainActivity, add this line: {code}" without specifying where exactly
- Gemini and ChatGPT-o1 often suggested very similiar solutions to error messages, as a result I ended up mainly using Gemini for that, as it is nicely embedded into Android Studio and thus easier to use.
- I also started using Gemini more often for smaller changes, like explaining me why certain modules won't work. A nice bonus was, that it gave links to related stackoverflow or documentation websites as reference, so i could read for myself. This also helped with learning more than the explanations of chatgpt. 
