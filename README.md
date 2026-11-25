# easy-sudoku


## Name

easy-sudoku : Android application from my easy brand


## Description

Small Android application around sudoku.
   + main challenge is read a grid from a newspaper
   + display the grid and help the user to solve it
   + never build an automated solver, use less
   + but create some 'helpers' to guess a cell like a user should

## Technologies covered

- Kotlin
- KSP
- Compose
- Coroutines
- Flow
- Room (database ORM)
- Architecture (database-dao-repository-viewmodel)
- Hilt (injection framework)
- Navigation 3
- Coil (Async Image)
- External App call (Document, Camera)

- Internal Camera
- Opencv for image processing
- Tesseract for OCR on the newspaper grid

## Special tricks

Basically inspired by other projects :
- usually old Project
- mainly in java
- some for desktop
- some for Android 

## Dependencies

- Mainly my beloved android lib (all standard required libs)
- Camera X
- OpenCV
- Tesseract

### In libs.version.toml
    [versions]
    hassenforder-android = "XX"

    [libraries]
    fr-uha-android = { module = "fr.uha.hassenforder:easy.android", version.ref = "hassenforder.android" }

### In settings-gradle.kts

    dependencyResolutionManagement {
       repositories {
            ...
            maven(url = "https://maven.pkg.github.com/hassenforder/easy-android") {
                credentials {
                    username = providers.gradleProperty("github-user").get()
                    password = providers.gradleProperty("github-pat").get()
                }
            }
            maven(
                url = "https://jitpack.io"
            )
            ...
        }
    }

## Reading Processing

This is/was the main challenge. Some preliminary checks show that,
recognize the whole grid with one OCR call is a pain.
- Some digits are missing
- Some others are wrong
- Some empty cells carry values

The ratio success over wrong is not enough to recognize and just ask the user to correct what is wrong.

Another processing was proposed :
- scan the camera to find the lines (opencv + hough transform)
- in the loop we try to help the user to put the camera at the right place without extra corrections :
-- zooming, rotation, panning, tilting, warping, 
-- the idea to help the user we display expected lines in red until we match in green
-- in green we can be pretty sure to find something if lighting is enough
- ask the user to move the camera until (all) lines are detected
- chop the image in 9x9 cells to recognize using OCR
- filter the result to digit only (if a lot of noise we have much different results)
- send the cell digit in a grid so the user can latelly correct what remains wrong


## License

Copyright © 2026 Hassenforder

Software released under the MIT license.

See the LICENSE file for more information.

https://opensource.org/licenses/MIT

## Project status

Ongoing
