# *Dump-And-Learn*

*Dump-And-Learn* is an easy java app that helps you learn any
language by yourself using media, such as listening to lyrics from a song,
or reading a book. The app encourages you save vocabulary when
you discover new words, and allows you to review them, based on
your preferences.

## Installation

Clone the repo:
```
git clone https://github.com/spyguerre/DumpAndLearn
cd DumpAndLearn
```

Install the Python dependencies in a Virtual Environment:
```
python -m venv .venv
.venv/Scripts/activate
pip install -r requirements.txt
```

Compile and run the project:
```
mvnw javafx:run
```

You can now **select your desired languages** in the Settings found in
the MenuBar on top; and start learning!

If your **desired language isn't listed**, feel free to add it!
You can add almost any language you want to access all the features of
the app:
- First, head to
[the tesseract OCR data repo](https://github.com/tesseract-ocr/tessdata),
find the data file corresponding to your language, and drop it under local
`./tessdata`.
- Then, find `src/main/java/dal/data/Languages.java`, and add
your language:
    - In the **enum**;
    - In the **getStdCode()** method (the String must match the
  two-letters country code of your language in google translate's url);
    - In the **getTessCode()** method (the String must match the first few
  letters before `.traineddata` of the file you added in `./tessdata`).

## Features

Dump-And-Learn has 4 main features, that are available directly from
the Main Menu.

### Manage Words

A basic menu that lets you easily add words to your personal learning
database, and search for them in it!

This menu also allows you to translate words from your selected native
language to your selected foreign language, and the other way around.

### Review Words

Here, you can start a review session depending on your needs; the
app allows you to select how many words you'd like to review,
which type of words you'd like to focus on (new,
old, often failed, or random), and in which language you'd like to
be asked to type the words.

The app provides a correction for each session, and encourages you
to correct the words that you misspelled until you get them right.

### Learn from a Song

This third feature allows you to type the name of a song and its artist,
to automatically download its lyrics from Genius (if available), and
its music video from youtube!

You can then highlight words that you don't understand from the lyrics,
and get an instant translation; with the ability to add these words
dynamically to your word bank.

### Scan Text

And finally, you can also scan text in your target language! Take
a picture of a book from your camera (or your phone,
using an app like Camo Studio or IVCam), and translate the
selected text instantly. You can also dynamically add words to your
word bank by highlighting any word once scanned.

The method is quite sensitive though, so make sure the selected text
is exactly horizontal, cropped right (you can crop
the picture once taken), and as readable as possible.
