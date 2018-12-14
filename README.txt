Instructions to execute the program:
Command line arguments:
[0] -> path of character relations file
[1] -> path of books folder
[2] -> options:
    p -> parse books not already parsed (i.e for which there is no .json file in the processedBooks folder)
        a -> use anaphora resolution with Stanford library (Caution: extremely slow)
        h -> use anaphora resolution with Hobbs algorithm (Caution: slow)
        f -> force reparse of all books
    b -> build Naive Bayes model with all books
    l -> load Naive Bayes model from file
    x -> do n-fold validation (model is not saved to file)
[3] [only with b, x or l option] label type to use in classifier. {affinity, coarse, fine}
[4] [only with x] number of folds for n-fold validation

Sample command line arguments:

    IMPORTANT: modify folder separator according to operating system

    Reparse all books without anaphora resolution/with stanford solver/with hobbs algorithm (takes more than a couple
    hours, especially with anaphora resolution)
    character_relation_annotations.txt ./TrainingBooks/ -pf
    character_relation_annotations.txt ./TrainingBooks/ -pfa
    character_relation_annotations.txt ./TrainingBooks/ -pfh

    Perform 3-fold cross validation loading books from .json files and using affinity/coarse/fine label
    character_relation_annotations.txt ./TrainingBooks/ -x affinity 3
    character_relation_annotations.txt ./TrainingBooks/ -x coarse 3
    character_relation_annotations.txt ./TrainingBooks/ -x fine 3

