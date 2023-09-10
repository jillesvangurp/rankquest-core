package com.jilesvangurp.rankquest.core.plugins

import com.jilesvangurp.rankquest.core.DEFAULT_JSON
import com.jillesvangurp.ktsearch.SearchResponse
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldNotStartWith
import kotlin.test.Test

class ElasticsearchPluginTest {

    @Test
    fun shouldExtractLabels() {
        val response = DEFAULT_JSON.decodeFromString(SearchResponse.serializer(), sampleResponse)
        response.hits?.hits?.forEach { hit ->

            listOf("title", "author.name").parseLabel(hit).let { l ->
                l shouldNotBe null
                l shouldNotContain "{"
            }
        }
    }
}

val sampleResponse = """
{
	"took": 3,
	"timed_out": false,
	"_shards": {
		"total": 1,
		"successful": 1,
		"skipped": 0,
		"failed": 0
	},
	"hits": {
		"total": {
			"value": 11,
			"relation": "eq"
		},
		"max_score": 1.0,
		"hits": [{
			"_index": "recipes",
			"_id": "banana-oatmeal-cookie.json",
			"_score": 1.0,
			"_source": {
				"title": "Banana Oatmeal Cookie",
				"description": "This recipe has been handed down in my family for generations. It's a good way to use overripe bananas. It's also a moist cookie that travels well either in the mail or car.",
				"ingredients": ["1 1/2 cups sifted all-purpose flour", "1/2 teaspoon baking soda", "1 teaspoon salt", "1/4 teaspoon ground nutmeg", "3/4 teaspoon ground cinnamon", "3/4 cup shortening", "1 cup white sugar", "1 egg", "1 cup mashed bananas", "1 3/4 cups quick cooking oats", "1/2 cup chopped nuts"],
				"directions": ["Preheat oven to 400 degrees F (200 degrees C).", "Sift together the flour, baking soda, salt, nutmeg and cinnamon.", "Cream together the shortening and sugar; beat until light and fluffy. Add egg, banana, oatmeal and nuts. Mix well.", "Add dry ingredients, mix well and drop by the teaspoon on ungreased cookie sheet.", "Bake at 400 degrees F (200 degrees C) for 15 minutes or until edges turn lightly brown. Cool on wire rack. Store in a closed container."],
				"servings": 24,
				"tags": ["dessert", "fruit"],
				"author": {
					"name": "Blair Bunny",
					"url": "http://allrecipes.com/cook/10179/profile.aspx"
				},
				"source_url": "http://allrecipes.com/Recipe/Banana-Oatmeal-Cookie/Detail.aspx"
			}
		}, {
			"_index": "recipes",
			"_id": "basil-and-pesto-hummus.json",
			"_score": 1.0,
			"_source": {
				"title": "Basil and Pesto Hummus",
				"description": "This deliciously-sweet hummus is bursting with basil and an all-around pesto body.",
				"ingredients": ["1 (16 ounce) garbanzo beans (chickpeas), drained and rinsed", "1/2 cup basil leaves", "1 clove garlic", "1 tablespoon olive oil", "1/2 teaspoon balsamic vinegar", "1/2 teaspoon soy sauce", "salt and ground black pepper to taste"],
				"directions": ["Combine the garbanzo beans, basil, and garlic in a food processor; pulse several times. Use a spatula to push mixture from sides of processor bowl.", "Pulse the mixture again while drizzling in the olive oil.", "Add the vinegar and soy sauce; pulse until combined.", "Season with salt and pepper."],
				"prep_time_min": 10,
				"servings": 5,
				"tags": ["appetizer", "snack", "dip", "spread", "vegetarian"],
				"author": {
					"name": "Fantastic Dan",
					"url": "http://allrecipes.com/cook/10167621/profile.aspx"
				},
				"source_url": "http://allrecipes.com/Recipe/Basil-and-Pesto-Hummus/Detail.aspx"
			}
		}, {
			"_index": "recipes",
			"_id": "black-bean-and-rice-enchiladas.json",
			"_score": 1.0,
			"_source": {
				"title": "Black Bean and Rice Enchiladas",
				"description": "These vegetarian black bean and rice enchiladas are just as satisfying as those served in restaurants.",
				"ingredients": ["1 tablespoon olive oil", "1 green bell pepper, chopped", "1 onion, chopped", "3 cloves garlic, minced", "1 (15 ounce) can black beans, rinsed and drained", "1 (14.5 ounce) can diced tomatoes and green chilies", "1/4 cup picante sauce", "1 tablespoon chili powder", "1 teaspoon ground cumin", "1/4 teaspoon red pepper flakes", "2 cups cooked brown rice", "8 (6 inch) flour tortillas, warmed", "1 cup salsa", "1 cup shredded Cheddar cheese", "3 tablespoons chopped fresh cilantro leaves", "1/4 cup shredded Cheddar cheese"],
				"directions": ["Preheat oven to 350 degrees F (175 degrees C). Lightly grease a 9x13-inch baking dish.", "Heat oil in a large skillet over medium heat; cook and stir green pepper, onion, and garlic until tender, about 7 minutes. Stir in beans, tomatoes, picante sauce, chili powder, cumin, and red pepper flakes; bring to a boil. Reduce heat to low and simmer, uncovered, until heated through and mixture thickens, about 5 minutes. Fold in rice and 1 cup Cheddar cheese; cook until heated through, about 5 minutes.", "Spoon a rounded 1/2 cup bean mixture down the center of each tortilla. Fold sides over filling and roll up. Place enchiladas seam side down in baking dish; spoon salsa over each tortilla. Cover baking dish with aluminum foil.", "Bake in preheated oven for 25 minutes. Uncover and sprinkle with cilantro and 1/4 cup Cheddar cheese. Bake until cheese is melted, 2 to 3 minutes."],
				"prep_time_min": 15,
				"cook_time_min": 50,
				"servings": 8,
				"tags": ["main dish", "vegetarian"],
				"author": {
					"name": "Diana Manzella-Miller",
					"url": "http://allrecipes.com/cook/13191855/profile.aspx"
				},
				"source_url": "http://allrecipes.com/Recipe/Black-Bean-and-Rice-Enchiladas-2"
			}
		}, {
			"_index": "recipes",
			"_id": "divine-hard-boiled-eggs.json",
			"_score": 1.0,
			"_source": {
				"title": "Divine Hard-Boiled Eggs",
				"description": "These hard-boiled eggs are cooked perfectly every time without turning the yolks green.",
				"ingredients": ["12 eggs"],
				"directions": ["Place eggs in a pot; pour enough water over the eggs to cover.", "Cover and turn stove to high; bring to a boil; turn off heat and place pot on a cool burner. Let the pot sit with the cover on for 15 minutes.", "Meanwhile, fill a large bowl halfway with cold water; transfer the eggs from the pot to the cold water. Replace the water with cold water as needed to keep cold until the eggs are completely cooled.", "Chill in refrigerator at least 2 hours before peeling."],
				"prep_time_min": 5,
				"cook_time_min": 15,
				"servings": 12,
				"tags": ["appetizer", "snack", "breakfast"],
				"author": {
					"name": "Rocky Road",
					"url": "http://allrecipes.com/cook/13810306/profile.aspx"
				},
				"source_url": "http://allrecipes.com/recipe/divine-hard-boiled-eggs/detail.aspx"
			}
		}, {
			"_index": "recipes",
			"_id": "four-cheese-margherita-pizza.json",
			"_score": 1.0,
			"_source": {
				"title": "Four Cheese Margherita Pizza",
				"description": "This is a fantastic version of an Italian classic. The feta cheese adds a rich flavor that brings this dish to life. Incredibly easy and incredibly delicious!",
				"ingredients": ["1/4 cup olive oil", "1 tablespoon minced garlic", "1/2 teaspoon sea salt", "8 Roma tomatoes, sliced", "2 (12 inch) pre-baked pizza crusts", "8 ounces shredded Mozzarella cheese", "4 ounces shredded Fontina cheese", "10 fresh basil leaves, washed, dried", "1/2 cup freshly grated Parmesan cheese", "1/2 cup crumbled feta cheese"],
				"directions": ["Stir together olive oil, garlic, and salt; toss with tomatoes, and allow to stand for 15 minutes.", "Preheat oven to 400 degrees F (200 degrees C).", "Brush each pizza crust with some of the tomato marinade. Sprinkle the pizzas evenly with Mozzarella and Fontina cheeses. Arrange tomatoes overtop, then sprinkle with shredded basil, Parmesan, and feta cheese.", "Bake in preheated oven until the cheese is bubbly and golden brown, about 10 minutes."],
				"prep_time_min": 15,
				"cook_time_min": 10,
				"servings": 8,
				"tags": ["main dish"],
				"author": {
					"name": "Michelle",
					"url": "http://allrecipes.com/cook/18668259/profile.aspx"
				},
				"source_url": "http://allrecipes.com/recipe/four-cheese-margherita-pizza"
			}
		}, {
			"_index": "recipes",
			"_id": "homemade-black-bean-veggie-burgers.json",
			"_score": 1.0,
			"_source": {
				"title": "Homemade Black Bean Veggie Burgers",
				"description": "You will never want to eat frozen veggie burgers again. These are so easy, and you'll be proud to have created such a vegetarian delight.",
				"ingredients": ["1 (16 ounce) can black beans, drained and rinsed", "1/2 green bell pepper, cut into 2 inch pieces", "1/2 onion, cut into wedges", "3 cloves garlic, peeled", "1 egg", "1 tablespoon chili powder", "1 tablespoon cumin", "1 teaspoon Thai chili sauce or hot sauce", "1/2 cup bread crumbs"],
				"directions": ["If grilling, preheat an outdoor grill for high heat, and lightly oil a sheet of aluminum foil. If baking, preheat oven to 375 degrees F (190 degrees C), and lightly oil a baking sheet.", "In a medium bowl, mash black beans with a fork until thick and pasty.", "In a food processor, finely chop bell pepper, onion, and garlic. Then stir into mashed beans.", "In a small bowl, stir together egg, chili powder, cumin, and chili sauce.", "Stir the egg mixture into the mashed beans. Mix in bread crumbs until the mixture is sticky and holds together. Divide mixture into four patties.", "If grilling, place patties on foil, and grill about 8 minutes on each side. If baking, place patties on baking sheet, and bake about 10 minutes on each side."],
				"prep_time_min": 15,
				"cook_time_min": 20,
				"servings": 4,
				"tags": ["main dish", "vegetarian", "grill"],
				"author": {
					"name": "Lauren Mu",
					"url": "http://allrecipes.com/cook/1445297/profile.aspx"
				},
				"source_url": "http://allrecipes.com/Recipe/Homemade-Black-Bean-Veggie-Burgers/Detail.aspx"
			}
		}, {
			"_index": "recipes",
			"_id": "homemade-chicken-enchiladas.json",
			"_score": 1.0,
			"_source": {
				"title": "Homemade Chicken Enchiladas",
				"description": "These enchiladas are great. Even my 5 year old loves them!",
				"ingredients": ["1 tablespoon olive oil", "2 cooked chicken breasts, shredded", "1 onion, diced", "1 green bell pepper, diced", "1 1/2 cloves garlic, chopped", "1 cup cream cheese", "1 cup shredded Monterey Jack cheese", "1 (15 ounce) can tomato sauce", "1 tablespoon chili powder", "1 tablespoon dried parsley", "1 teaspoon dried oregano", "1/2 teaspoon salt", "1/2 teaspoon ground black pepper", "8 (10 inch) flour tortillas", "2 cups enchilada sauce", "1 cup shredded Monterey Jack cheese"],
				"directions": ["Preheat oven to 350 degrees F (175 degrees C).", "Heat olive oil in a skillet over medium heat. Cook and stir chicken, onion, green bell pepper, garlic, cream cheese, and 1 cup Monterey Jack cheese in hot oil until the cheese melts, about 5 minutes. Stir tomato sauce, chili powder, parsley, oregano, salt, and black pepper into the chicken mixture.", "Divide mixture evenly into tortillas, roll the tortillas around the filling, and arrange in a baking dish. Cover with enchilada sauce and remaining 1 cup Monterey Jack cheese.", "Bake in preheated oven until cheese topping melts and begins to brown, about 15 minutes."],
				"prep_time_min": 15,
				"cook_time_min": 20,
				"servings": 8,
				"tags": ["main dish"],
				"author": {
					"name": "Mary Kate",
					"url": "http://allrecipes.com/cook/14977239/profile.aspx"
				},
				"source_url": "http://allrecipes.com/Recipe/Homemade-Chicken-Enchiladas/Detail.aspx"
			}
		}, {
			"_index": "recipes",
			"_id": "marinated-grilled-shrimp.json",
			"_score": 1.0,
			"_source": {
				"title": "Marinated Grilled Shrimp",
				"description": "A very simple and easy marinade that makes your shrimp so yummy you don't even need cocktail sauce! Don't let the cayenne pepper scare you, you don't even taste it. My 2 and 4 year-olds love it and eat more shrimp than their parents! It is also a big hit with company, and easy to prepare. I make this with frozen or fresh shrimp and use my indoor electric grill if the weather is not good for outside grilling. Try it with a salad, baked potato, and garlic bread. You will not be disappointed!!!",
				"ingredients": ["3 cloves garlic, minced", "1/3 cup olive oil", "1/4 cup tomato sauce", "2 tablespoons red wine vinegar", "2 tablespoons chopped fresh basil", "1/2 teaspoon salt", "1/4 teaspoon cayenne pepper", "2 pounds fresh shrimp, peeled and deveined", "skewers"],
				"directions": ["In a large bowl, stir together the garlic, olive oil, tomato sauce, and red wine vinegar. Season with basil, salt, and cayenne pepper. Add shrimp to the bowl, and stir until evenly coated. Cover, and refrigerate for 30 minutes to 1 hour, stirring once or twice.", "Preheat grill for medium heat. Thread shrimp onto skewers, piercing once near the tail and once near the head. Discard marinade.", "Lightly oil grill grate. Cook shrimp on preheated grill for 2 to 3 minutes per side, or until opaque."],
				"prep_time_min": 15,
				"cook_time_min": 6,
				"servings": 6,
				"tags": ["main dish", "grill"],
				"author": {
					"name": "Blondie Perez",
					"url": "http://allrecipes.com/cook/1804621/profile.aspx"
				},
				"source_url": "http://allrecipes.com/recipe/marinated-grilled-shrimp"
			}
		}, {
			"_index": "recipes",
			"_id": "vegetable-fried-rice.json",
			"_score": 1.0,
			"_source": {
				"title": "Vegetable Fried Rice",
				"description": "This dish combines the nutty flavor of brown rice with the fresh taste of bell peppers, baby peas, and other vegetables.",
				"ingredients": ["3 cups water", "1 1/2 cups quick-cooking brown rice", "2 tablespoons peanut oil", "1 small yellow onion, chopped", "1 small green bell pepper, chopped", "1 teaspoon minced garlic", "1/4 teaspoon red pepper flakes", "3 green onions, thinly sliced", "3 tablespoons soy sauce", "1 cup frozen petite peas", "2 teaspoons sesame oil", "1/4 cup roasted peanuts (optional)"],
				"directions": ["In a saucepan bring water to a boil. Stir in rice. Reduce heat, cover and simmer for 20 minutes.", "Meanwhile, heat peanut oil in a large skillet or wok over medium heat. Add onions, bell pepper, garlic and pepper flakes, to taste. Cook 3 minutes, stirring occasionally.", "Increase heat to medium-high and stir in cooked rice, green onions and soy sauce. Stir-fry for 1 minute. Add peas and cook 1 minute more. Remove from heat. Add sesame oil and mix well. Garnish with peanuts, if desired."],
				"prep_time_min": 15,
				"cook_time_min": 40,
				"servings": 4,
				"tags": ["main dish", "vegetarian"],
				"author": {
					"name": "Dakota Kelly",
					"url": "http://allrecipes.com/cook/1223369/profile.aspx"
				},
				"source_url": "http://allrecipes.com/Recipe/Vegetable-Fried-Rice/Detail.aspx"
			}
		}, {
			"_index": "recipes",
			"_id": "vegetarian-korma.json",
			"_score": 1.0,
			"_source": {
				"title": "Vegetarian Korma",
				"description": "This is an easy and exotic Indian dish. It's rich, creamy, mildly spiced, and extremely flavorful. Serve with naan and rice.",
				"ingredients": ["1 1/2 tablespoons vegetable oil", "1 small onion, diced", "1 teaspoon minced fresh ginger root", "4 cloves garlic, minced", "2 potatoes, cubed", "4 carrots, cubed", "1 fresh jalapeno pepper, seeded and sliced", "3 tablespoons ground unsalted cashews", "1 (4 ounce) can tomato sauce", "2 teaspoons salt", "1 1/2 tablespoons curry powder", "1 cup frozen green peas", "1/2 green bell pepper, chopped", "1/2 red bell pepper, chopped", "1 cup heavy cream", "1 bunch fresh cilantro for garnish"],
				"directions": ["Heat the oil in a skillet over medium heat. Stir in the onion, and cook until tender.", "Mix in ginger and garlic, and continue cooking 1 minute.", "Mix potatoes, carrots, jalapeno, cashews, and tomato sauce. Season with salt and curry powder. Cook and stir 10 minutes, or until potatoes are tender.", "Stir peas, green bell pepper, red bell pepper, and cream into the skillet. Reduce heat to low, cover, and simmer 10 minutes.", "Garnish with cilantro to serve."],
				"prep_time_min": 25,
				"cook_time_min": 30,
				"servings": 4,
				"tags": ["main dish", "vegetarian", "indian"],
				"author": {
					"name": "Yakuta",
					"url": "http://allrecipes.com/cook/116722/profile.aspx"
				},
				"source_url": "http://allrecipes.com/Recipe/Vegetarian-Korma/Detail.aspx"
			}
		}]
	}
}    
""".trimIndent()