# Supervised Characters Relationship Extraction from Stories
The aim of this project is to buid a supervised algorithm able to automatically find the story characters, and subsequentially extract the relationship between them given a book in UTF-8 format. Relationships can be retrieved with different degrees of granularity:
* Affinity (Positive, Neutral, Negative): this indicates in a very generic way what is the nature of a connection between two characters.
* Coarse Grained Category (Social, Familial, Professional): this is still a generic categorization that is able to indicate in which sphere of social interaction the relationship is formed.
* Fine Grained Category (Friend, Lover, Parent, etc): this is the most specific description that we can achieve, describing pretty accurately what is the type of relationship. 

All of this is achieved through an extensive pre-processing process of the corpus (involving NER and POS tagging, Lemmatization, Anaphora resolution and many other operations) followed by a Naive Bayes model classification.
One of the main problems was the retrieveal of story characters that are cited with different forms (prefixes, nicknames, abbreviations, ...), and this was solved by using a custom approach that tries to minimize the variability of the appearing names using only sentences for which the NER tagger signalled the presence of human entities.

Training is executed using 109 books that can be found for free in Project Guthenberg:
https://www.gutenberg.org/

## Results
Performance of the algorithm was evaluated using n-Fold Cross validation. Results were very outstanding, reaching 79% precision for coarse relations (resulting in a 10% increase of precision over the state of the art algorithms). Those results could be imporved bu using a better set of labels for the training books (which often did not consider all possible characters appearing in the book).

## Documentation
Detailed documentation about the agorithm can be found in the following paper:
https://drive.google.com/file/d/1gzAz3IS8_ZmKVkvYRQAek1lBEz30DJFX/view?usp=sharing

## Adopted libraries
The following libraries are required in order to run the program:
* Stanford Tokenizer: https://nlp.stanford.edu/software/tokenizer.shtml
* Stanford NER: https://nlp.stanford.edu/software/CRF-NER.shtml
* Stanford Coref Annotator: https://stanfordnlp.github.io/CoreNLP/coref.html
