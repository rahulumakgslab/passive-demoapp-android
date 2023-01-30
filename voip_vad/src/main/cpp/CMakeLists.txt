# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("voip_vad")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.


include_directories(global_h h)
add_library( # Sets the name of the library.
        voip_vad

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        voip_vad.c
        ./src/main/sonde_sfe_elck_init.c
        ./src/main/sonde_sp_elck_driver.c
        ./src/main/main.c
        ./src/preprocessing/sonde_sfe_preprocessing.c
        ./src/preprocessing/sonde_sfe_mem_init.c
        ./src/vad/sonde_sfe_vad_proc.c
        ./src/vad/sonde_sfe_vad_mathops.c
        ./src/signal/sonde_sfe_optfft.c
        ./src/signal/sonde_sfe_optsigproc.c
        ./src/preprocessing/sonde_sfe_config_fileread.c
        ./src/preprocessing/sonde_sfe_waveread.c
        ./src/signal/sonde_sfe_resample.c
        ./src/signal/resample_fft.c
        ./src/signal/sonde_sfe_fft.c
        ./src/signal/sonde_sfe_sma_filter.c
        ./src/elicitation_detection/sonde_sfe_elicitation_detection.c
        ./src/elicitation_detection/sonde_sfe_filters.c
        ./src/energy_meter/sonde_sfe_energy_meter.c
#        ./src/summary/summary.h
#        ./src/summary/sum.c
#        ./src/summary/rolling_mean.c
#        ./src/free_speech/sonde_sfe_free_speech.c
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        voip_vad

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})