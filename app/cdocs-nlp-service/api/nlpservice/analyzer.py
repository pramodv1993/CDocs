import spacy

nlp = spacy.load("en_core_web_sm")
def analyze(text):
	result = {}
	doc = nlp(text)
	nouns = [chunk.text for chunk in doc.noun_chunks]
	verbs = [token.lemma_ for token in doc if token.pos_=="VERB"]
	entities = {}
	for entity in doc.ents:
		entities[entity.text] = entity.label_
	
	result['nouns'] = nouns
	result['verbs'] = verbs
	result['entities'] = entities

	return result


