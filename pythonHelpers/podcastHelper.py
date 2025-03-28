import sys
import wave
import json
import os
import yt_dlp
from vosk import Model, KaldiRecognizer
import ffmpeg
import zipfile


def transcribe_wav_to_srt(wav_file, srt_fp, model_path):
    # Load Vosk model
    model = Model(model_path)

    # Open the WAV file
    wf = wave.open(wav_file, "rb")

    # Initialize recognizer
    recognizer = KaldiRecognizer(model, wf.getframerate())

    # Get audio data
    audio_data = wf.readframes(wf.getnframes())

    # Open the output SRT file
    with open(srt_fp, "w", encoding="UTF-8") as srt_file:
        index = 1
        start_time = 0
        sentence = ""

        # Iterate through audio data and recognize speech
        for i in range(0, len(audio_data), 4000):  # 4000 frames is a good chunk size for Vosk
            chunk = audio_data[i:i + 4000]
            if recognizer.AcceptWaveform(chunk):
                result = recognizer.Result()
                result_json = json.loads(result)
                if 'text' in result_json:
                    sentence += " " + result_json['text']
                    # Calculate time range (in seconds) for the subtitles
                    end_time = (i + len(chunk)) / wf.getframerate()
                    # Write subtitle in SRT format
                    srt_file.write(f"{index}\n")
                    srt_file.write(f"{format_time(start_time / 2)} --> {format_time(end_time / 2)}\n")
                    srt_file.write(f"{sentence.strip()}\n\n")
                    index += 1
                    start_time = end_time  # Set the start time for the next subtitle
                    sentence = ""  # Reset sentence after writing to SRT file

    print(f"Transcription complete. Subtitle file saved as {srt_fp}")


def format_time(seconds):
    """Convert time in seconds to SRT time format (HH:MM:SS,SSS)"""
    hours = int(seconds // 3600)
    minutes = int((seconds % 3600) // 60)
    seconds = seconds % 60
    milliseconds = int((seconds - int(seconds)) * 1000)
    return f"{hours:02}:{minutes:02}:{int(seconds):02},{milliseconds:03}"


def convert_mp4_to_wav(input_file, output_file):
    """Convert an MP4 file to WAV format using ffmpeg-python."""
    try:
        (ffmpeg.input(input_file).output(output_file, format='wav', acodec='pcm_s16le', ac=1, ar='16000')
         .run(overwrite_output=True))
        print(f"Conversion successful: {output_file}")
    except ffmpeg.Error as e:
        print(f"Error during conversion: {e}")


def unzip_model(language):
    """Unzip the specified Vosk model."""
    # Unzip the model
    with zipfile.ZipFile(f"voskModels/{language}.zip", 'r') as zip_ref:
        extracted_files = zip_ref.namelist()
        zip_ref.extractall("voskModels")

    # Rename the extracted folder to the language name
    language_folder = extracted_files[0].split('/')[0]
    os.rename(f"voskModels/{language_folder}", f"voskModels/{language}")


if __name__ == "__main__":
    if len(sys.argv) != 4:
        print("Usage: podcastHelper.py <yt_code> <language> <dl_id>")
        sys.exit(1)

    # Retrieve command line arguments
    yt_code = sys.argv[1]
    language = sys.argv[2]
    dl_id = sys.argv[3]

    # Unzip the specified model if it isn't already unzipped
    if not os.path.exists(f"voskModels/{language}"):
        if f"{language}.zip" not in os.listdir("voskModels"):
            print(f"Model for language {language} not found. Please download the model first, drop it in voskModels/, and rename it to the two-letters language code of your language.")
            sys.exit(1)

        unzip_model(language)

    # Create downloads directory if it doesn't exist
    if not os.path.exists("downloads/podcasts"):
        os.makedirs("downloads/podcasts")

    # Define file paths
    mp4_file = f"downloads/podcasts/{dl_id}.mp4"
    wav_file = f"downloads/podcasts/{dl_id}.wav"
    srt_file = f"downloads/podcasts/{dl_id}.srt"
    model_path = f"voskModels/{language}"

    print(f"Downloading the MP4 podcast at \"https://youtube.com/watch?v={yt_code}\"...")

    # Download MP4 using yt-dlp
    with yt_dlp.YoutubeDL({'format': 'mp4', 'outtmpl': mp4_file}) as ydl:
        ydl.download([yt_code])

    print("Download complete. Converting the MP4 podcast to WAV format...")

    # Convert MP4 to WAV
    convert_mp4_to_wav(mp4_file, wav_file)

    print("Conversion complete. Transcribing the WAV podcast to SRT subtitles...")

    # Transcribe WAV to SRT
    transcribe_wav_to_srt(wav_file, srt_file, model_path)

    print("Transcription complete. Deleting WAV file...")

    # Delete WAV file
    os.remove(wav_file)

    print(f"Podcast {dl_id} ready!")
