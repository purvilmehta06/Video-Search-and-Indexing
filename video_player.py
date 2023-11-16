from PyQt5.QtCore import Qt, QUrl, QSize
from PyQt5.QtMultimedia import QMediaContent, QMediaPlayer
from PyQt5.QtMultimediaWidgets import QVideoWidget
from PyQt5.QtWidgets import (
    QApplication,
    QHBoxLayout,
    QPushButton,
    QSlider,
    QStyle,
    QVBoxLayout,
    QWidget,
)
import os
import sys
import signal


class VideoPlayer(QWidget):
    def __init__(self, file_name, start_frame):
        # ============================ Default Configs ========================
        super(VideoPlayer, self).__init__(None)
        self.file_name = file_name
        self.start_frame = start_frame
        self.media_player = QMediaPlayer(None, QMediaPlayer.VideoSurface)
        self.media_player.setMedia(QMediaContent(QUrl.fromLocalFile(self.file_name)))

        # ============================ UI elements ============================
        videoWidget = QVideoWidget()

        self.reset_button = QPushButton("Reset")
        self.reset_button.setFixedHeight(30)
        self.reset_button.clicked.connect(self.reset)

        self.play_button = QPushButton()
        self.play_button.setFixedHeight(30)
        self.play_button.setIconSize(QSize(20, 20))
        self.play_button.setIcon(self.style().standardIcon(QStyle.SP_MediaPlay))
        self.play_button.clicked.connect(self.play)

        self.position_slider = QSlider(Qt.Horizontal)
        self.position_slider.setRange(0, 0)
        self.position_slider.sliderMoved.connect(self.set_position)
        self.position_slider.sliderPressed.connect(self.on_slider_pressed)

        control_layout = QHBoxLayout()
        control_layout.setContentsMargins(0, 0, 0, 0)
        control_layout.addWidget(self.reset_button)
        control_layout.addWidget(self.play_button)
        control_layout.addWidget(self.position_slider)

        layout = QVBoxLayout()
        layout.addWidget(videoWidget)
        layout.addLayout(control_layout)
        self.setLayout(layout)

        # ============================ Set Media Player =======================
        self.media_player.setVideoOutput(videoWidget)
        self.media_player.positionChanged.connect(self.positionChanged)
        self.media_player.durationChanged.connect(self.duration_changed)
        self.media_player.error.connect(self.handle_error)

        # ============================ Set Start Frame ========================
        self.play()
        self.play()
        self.media_player.setPosition(start_frame)

    def reset(self):
        if self.media_player.state() == QMediaPlayer.PlayingState:
            self.play()
        self.media_player.setPosition(0)

    def play(self):
        if self.media_player.state() == QMediaPlayer.PlayingState:
            self.media_player.pause()
            self.play_button.setIcon(self.style().standardIcon(QStyle.SP_MediaPlay))
        else:
            self.media_player.play()
            self.play_button.setIcon(self.style().standardIcon(QStyle.SP_MediaPause))

    def on_slider_pressed(self):
        position = self.position_slider.value()
        self.media_player.setPosition(position)

    def positionChanged(self, position):
        self.position_slider.setValue(position)

    def duration_changed(self, duration):
        self.position_slider.setRange(0, duration)

    def set_position(self, position):
        self.media_player.setPosition(position)

    def handle_error(self):
        self.play_button.setEnabled(False)

    def closeEvent(self, event):
        self.media_player.stop()
        event.accept()


if __name__ == "__main__":
    signal.signal(signal.SIGINT, signal.SIG_DFL)
    dir = os.path.dirname(os.path.realpath(__file__))
    file_name = os.path.join(dir, sys.argv[1])
    start_frame = int(sys.argv[2])

    app = QApplication(sys.argv)
    player = VideoPlayer(file_name, int(start_frame * 1000 / 30))
    player.setWindowTitle("Video Player")
    player.resize(600, 400)
    player.show()
    sys.exit(app.exec_())
