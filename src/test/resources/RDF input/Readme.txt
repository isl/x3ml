01. Simple
``````````
Περιέχει απλές Class-2-Class συσχετίσεις. Πρακτικά ψάχνει όλα τα instances της κλάσης/property που δίνεται στο source (είτε μιλάμε για το source_path ενός domain ή range, είτε μιλάμε για το source_relation ενός range). 

02. Simple-Reuse
``````````
Περιέχει απλές Class-2-Class συσχετίσεις. Ομοίως με την περίπτωση 01.Simple, μόνο που αντί να δημιουργεί καινούρια IDs (URIs, UUIDs) επαναχρησιμοποιεί αυτά που υπάρχουν στο input.

03. Complex
``````````
Περιέχει συσχετίσεις μεταξύ ενός path στο RDF input (Node -> Relation -> Node ... ) σε μία κλάση στο output. Περιέχει διάφορα αρχεία με mappings όπου έχουν συνδυασμούς τέτοιων paths (στο domain, to path και το range)

04. Complex-Reuse
``````````
Όμοια με την περίπτωση 3 μόνο που αντί να δημιουργεί καινούρια IDs (URIs, UUIDs) επαναχρησιμοποιεί αυτά που υπάρχουν στο input.

05. Complex-AltInput
``````````
Παράδειγμα που χρησιμοποιεί ως input ένα αρχείο διαφορετική μορφή (NTRIPLES αντί για RDF)