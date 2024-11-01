# Curren-C
## Project Overview
This is a small project to experiment with building an Android app solely with ChatGPT-o1-preview (and a bit of Gemini, since it is currently included in Android Studio for free).
The main goal of the project is to see how hard or easy it is to build a simple app with an LLM compared to learning how to do it. I also will write down learnings from this experiment.
I used the [Android Basics with Compose Course](https://developer.android.com/courses/android-basics-compose/course) alongside sometimes, to atleast understand to some degree what I was using or doing, and also to understand how much effort it would be, to learn the steps and apply them. I'll write the app on my own then, using that course.

## App overview
The app shall be a minimal and easy to use app for converting currencies on the go. 
The app shall have three input fields which automatically update whenever you type in them and query an API (I used the [exchangerate-api](https://www.exchangerate-api.com/docs/java-currency-api) but plan on using [frankfurter](https://frankfurter.dev)) once a week to get the newest conversion rates and using cached ones otherwise. Currency codes json gladly taken from [here](https://github.com/ourworldincode/currency/tree/main)

## Learnings
- I gave detailed instructions and prompted to ask clarifying questions first before starting to code. ChatGPT-o1 obeys and asks good questions to clarify. I will use this approach in further projects too.
- ChatGPT-o1 is good at providing a project outline as well, and told me what to write into which file (~20 files) in one single answer. Even the layout .xml looked good on the first try.
- However, it did not perform good on adding dependencies, especially when prompted to use a [version libray](https://developer.android.com/build/dependencies#add-dependency). It seemed to sometimes get confused between Groovy and Kotlin. In the end I had to add them manually, a painful thing and I imagine it could be a dealbreaker for people who don't know anything about coding.
- I had to let it fix several things: dependencies, permissions, (add)logging, json parsing
- ChatGPT-o1 often assumed that I know where to put the code it suggests, saying things like "in MainActivity, add this line: {code}" without specifying where exactly, sometimes it lets you change the code in the same function twice, which is good for understanding whats happening but also quite annoying sometimes
- However, it's general coding performance is way better than the 4o model, it is much better in referencing created functions and keeping consistent with what has been coded so far (eg. not creating the same function but with different variable names and execution in another file)
- Gemini and ChatGPT-o1 often suggested very similiar solutions to error messages, as a result I ended up mainly using Gemini for that, as it is nicely embedded into Android Studio and thus easier to use.
- I also started using Gemini more often for smaller changes, like explaining me why certain modules won't work. A nice bonus was, that it gave links to related stackoverflow or documentation websites as reference, so i could read for myself. This also helped with learning more than the explanations of chatgpt. 

## Result
I got a working App in about 2 days, which does everything I wanted.

I am impressed at how much I was able to do, even though generating code using ChatGPT is less satisfying as coding on your own - essentially I had to explain a lot to it, and even though it almost always did what I needed it to, sometimes it felt like fixing stuff would've been easier if I knew the codebase and what was going on.

I knew that ChatGPT-o1 was also able to generate good .xml layouts and non-boilerplate code, but it's still impressing when you see it in action, also compared to experiences I had with coding with ChatGPT4o.

Through debugging together with Gemini and ChatGPT, I knew the structure of the code quite well in the end, even though I did write very little code on my own.
I guess it would've taken me about 1,5x the time if I would've built the app on my own, but I would've learned a lot more about Kotlin and Android App coding compared to this approach. One could argue that you don't need that knowledge if you can use an LLM for debugging etc., but I still believe it's easier and more fun to do the work oneself. It definitely is more satisfying.
