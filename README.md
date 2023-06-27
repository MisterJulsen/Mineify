# Mineify

![Logo](https://github.com/MisterJulsen/Mineify/blob/1.18.2/mineify_icon.png)
 
#### **With Mineify, each player can upload their own sounds or music to the server and let them play to any player at any position. The best thing about this mod is that no resource pack is required as the music is loaded directly from the server.**

FFmpeg is required for this mod to upload custom sounds and music! Download the FFmpeg binaries for your OS from https://ffmpeg.org and put them in a folder named "ffmpeg" inside your minecraft installation folder (default: .minecraft). The reason for this dependency is, that minecraft can only read ogg files. And for ease of use, you can select any audio format and it will be converted with FFmpeg automatically before uploading.

#### **Advantages compared to sound resource packs**
- Clients don't need an additional resource pack (where (re)loading can take a while especially in large modpacks).
- Sounds can be used immediately after uploading.
- Each player can upload their own sounds and music without all clients having to download a new resource pack.
- Streaming from the server.

#### **Features**
_Below are the features currently included in the mod. This list is not final yet, new content can be added at any time._

Players can ...
 - ... upload custom sounds and music files.
 - ... create playlists using the Sound Player Block.
 - ... define an area in which the sound should be played.
 - ... control the playback with redstone.
 - ... decide whether other players can also use the sounds or whether it is private

Server owners can ...
- ... provide custom sounds which are available for all players.
- ... manage uploaded files.
- ... control what players can do (using a config which is not included at the moment)
  
#### **Known limitations**
Due to the fact that the currently playing sound and music data needs to be stored somewhere, playing too many custom sounds at the same time may cause an out-of-memory crash. This limit may be reached faster if the available RAM is low and the sound files are very large.

#### **Supported Languages**
 - English (100%)
 - German (100%)

#### **Please note ...**
... when using this mod (especially **alpha** versions) that it can always contain bugs that could potentially **damage your world**! So please make a **backup** of your world before installing this mod.

#### **Supported Versions**
A **fabric** version of this mod is currently **not planned** and the mod is mainly ported to **newer versions**. Please don't ask when the update for version X will come out as I don't know myself. The update will come when I have time for it and it's ready.
