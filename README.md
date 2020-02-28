# Native Android AR Core Sceneform - .obj Animation
Test Augmented Reality (AR) Project to try and animate in native Android using ARCore with .obj files only. 

# Motivation
Native Android AR Core (at the time of writing this), currently only supports animations in .fbx format, this is great for animation that do not require intense mesh transformations. However, if you do, the only other option availble is trying to animation .obj files, as this is the only format supported by Native Android AR Core that can handle complex mesh transformations found in cloth simulations. On a side note - Alembic is designed to do this sort of thing, however, this format is not currently supported.

# Results
It was discovered that once more than 50 .obj were passed to the ARCore Model Loader, RAM started to struggle. When batching model-loading and completing 100 .obj models, RAM was floating around 1.5gb, then, when the AR activity starts, it goes over the 2GB limit causing a crash.

# Workaround
At the time of writing this, AR Core for native Android is still in Beta, therefore I hope these issues will not prolong for long. However, as mentioned earlier, I was interested in handling mesh transformations, therefore I decided to rebuild the AR element of the simulation using Unity. The great thing about this approch is that you can use your .obj directly, rather than having to pass the to a model loader. You can see my working example here https://github.com/ollyc2015/android_unity_shopping 
