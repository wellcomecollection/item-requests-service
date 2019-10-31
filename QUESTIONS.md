## Thoughts:
Add a note saying when a person wants something. Or `neededBy`.

## Questions
What makes an item requestable?
- It's not on the shelves

Can we not think about the cart?

What's average books person on request?

What would we want to surface on the client?
- Physical items and distinguishing features e.g. how to access them
- A status of the items requestability - is it already on hold?
- Ability to create a hold - and where the item will be available (rare reading room / library desk)



Structure

/items returns
```js
{
"status": "on Request",
//private
"requestedByMe": true
}
```
